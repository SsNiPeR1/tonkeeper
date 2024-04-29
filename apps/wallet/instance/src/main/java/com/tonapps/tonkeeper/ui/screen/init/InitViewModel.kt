package com.tonapps.tonkeeper.ui.screen.init

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.contract.WalletV4R2Contract
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.blockchain.ton.extensions.toWalletAddress
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.tonkeeper.ui.screen.init.list.AccountItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.AccountDetailsEntity
import com.tonapps.wallet.api.entity.AccountEntity
import com.tonapps.wallet.data.account.WalletColor
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.WalletSource
import com.tonapps.wallet.data.account.entities.WalletLabel
import com.tonapps.wallet.data.collectibles.CollectiblesRepository
import com.tonapps.wallet.data.collectibles.entities.NftEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.block.AddrStd
import org.ton.crypto.hex
import org.ton.mnemonic.Mnemonic

@OptIn(FlowPreview::class)
class InitViewModel(
    private val type: InitArgs.Type,
    application: Application,
    private val passcodeRepository: PasscodeRepository,
    private val walletRepository: WalletRepository,
    private val tokenRepository: TokenRepository,
    private val collectiblesRepository: CollectiblesRepository,
    private val settingsRepository: SettingsRepository,
    private val api: API,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(application) {

    private val savedState = InitModelState(savedStateHandle)
    private val testnet: Boolean = type == InitArgs.Type.Testnet

    private val _uiTopOffset = MutableStateFlow(0)
    val uiTopOffset = _uiTopOffset.asStateFlow()

    private val _eventFlow = MutableSharedFlow<InitEvent>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val eventFlow = _eventFlow.asSharedFlow().filterNotNull()

    private val _watchAccountResolveFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val watchAccountFlow = _watchAccountResolveFlow.asSharedFlow()
        .debounce(1000)
        .filter { it.isNotBlank() }
        .map {
            val account = api.resolveAddressOrName(it, testnet)
            if (account == null || !account.active) {
                setWatchAccount(null)
                return@map null
            }
            setWatchAccount(account)
            account
        }.flowOn(Dispatchers.IO)

    val accountsFlow = savedState.accountsFlow.filterNotNull()

    private val hasPushPermission: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return ContextCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
            return true
        }

    init {
        if (!passcodeRepository.hasPinCode) {
            _eventFlow.tryEmit(InitEvent.Step.CreatePasscode)
        } else {
            startWalletFlow()
        }
    }

    fun toggleAccountSelection(item: AccountItem) {
        val accounts = savedState.accounts?.toMutableList() ?: return
        val index = accounts.indexOfFirst { it.address == item.address }
        if (index == -1) {
            return
        }
        accounts[index] = item.copy(selected = !item.selected)
        savedState.accounts = accounts
    }

    private fun startWalletFlow() {
        if (type == InitArgs.Type.Watch) {
            _eventFlow.tryEmit(InitEvent.Step.WatchAccount)
        } else if (type == InitArgs.Type.New) {
            _eventFlow.tryEmit(InitEvent.Step.LabelAccount)
        } else if (type == InitArgs.Type.Import) {
            _eventFlow.tryEmit(InitEvent.Step.ImportWords)
        } else if (type == InitArgs.Type.Signer) {
            _eventFlow.tryEmit(InitEvent.Step.LabelAccount)
        }
    }

    fun setUiTopOffset(offset: Int) {
        _uiTopOffset.value = offset
    }

    fun routePopBackStack() {
        _eventFlow.tryEmit(InitEvent.Back)
    }

    fun resolveWatchAccount(value: String) {
        _watchAccountResolveFlow.tryEmit(value)
    }

    fun setPasscode(passcode: String) {
        savedState.passcode = passcode

        _eventFlow.tryEmit(InitEvent.Step.ReEnterPasscode)
    }

    fun reEnterPasscode(passcode: String) {
        val valid = savedState.passcode == passcode
        if (!valid) {
            routePopBackStack()
            return
        }
        startWalletFlow()
    }

    fun setMnemonic(words: List<String>) {
        savedState.mnemonic = words
        resolveWallets()
    }

    fun setPublicKey(publicKey: PublicKeyEd25519?) {
        savedState.publicKey = publicKey
    }

    private fun resolveWallets() {
        val mnemonic = savedState.mnemonic ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val seed = Mnemonic.toSeed(mnemonic)
            savedState.seed = seed
            val privateKey = PrivateKeyEd25519(seed)
            val publicKey = privateKey.publicKey()
            val accounts = api.resolvePublicKey(publicKey, testnet).filter {
                it.isWallet && it.walletVersion != WalletVersion.UNKNOWN && it.active
            }.sortedByDescending { it.walletVersion.index }.toMutableList()

            if (accounts.count { it.walletVersion == WalletVersion.V4R2 } == 0) {
                val contract = WalletV4R2Contract(publicKey = publicKey)
                accounts.add(0, AccountDetailsEntity(
                    query = "",
                    preview = AccountEntity(
                        address = contract.address.toWalletAddress(testnet),
                        accountId = contract.address.toAccountId(),
                        name = null,
                        iconUri = null,
                        isWallet = true,
                        isScam = false,
                    ),
                    active = true,
                    walletVersion = WalletVersion.V4R2,
                    balance = 0
                ))
            }

            val deferredTokens = mutableListOf<Deferred<List<AccountTokenEntity>>>()
            for (account in accounts) {
                deferredTokens.add(async { getTokens(account.address) })
            }

            val deferredCollectibles = mutableListOf<Deferred<List<NftEntity>>>()
            for (account in accounts) {
                deferredCollectibles.add(async { collectiblesRepository.getRemoteNftItems(account.address, testnet) })
            }

            val items = mutableListOf<AccountItem>()
            for ((index, account) in accounts.withIndex()) {
                val balance = Coin.toCoins(account.balance)
                val hasTokens = deferredTokens[index].await().size > 1
                val hasCollectibles = deferredCollectibles[index].await().isNotEmpty()
                val item = AccountItem(
                    address = AddrStd(account.address).toWalletAddress(testnet),
                    name = account.name,
                    walletVersion = account.walletVersion,
                    balanceFormat = CurrencyFormatter.format("TON", balance),
                    tokens = hasTokens,
                    collectibles = hasCollectibles,
                    selected = account.walletVersion == WalletVersion.V4R2 || (account.balance > 0 && hasTokens),
                    position = ListCell.getPosition(accounts.size, index)
                )
                items.add(item)
            }
            savedState.accounts = items

            if (items.size > 1) {
                _eventFlow.tryEmit(InitEvent.Step.SelectAccount)
            } else {
                _eventFlow.tryEmit(InitEvent.Step.LabelAccount)
            }
        }
    }

    private suspend fun getTokens(accountId: String): List<AccountTokenEntity> {
        return tokenRepository.getRemote(settingsRepository.currency, accountId, testnet)
    }

    private fun setWatchAccount(account: AccountDetailsEntity?) {
        val oldAccount = getWatchAccount()
        if (oldAccount?.equals(account) == true) {
            return
        }

        savedState.watchAccount = account
        setLabelName(account?.name ?: "")
    }

    fun getWatchAccount(): AccountDetailsEntity? {
        return savedState.watchAccount
    }

    fun getAccounts(): List<AccountItem> {
        return savedState.accounts ?: emptyList()
    }

    fun setLabel(name: String, emoji: String, color: Int) {
        setLabel(WalletLabel(name, emoji, color))
    }

    fun setLabel(label: WalletLabel) {
        savedState.label = label
    }

    fun getLabel(): WalletLabel {
        return savedState.label ?: WalletLabel("","\uD83D\uDE00", WalletColor.all.first())
    }

    fun setLabelName(name: String) {
        val oldLabel = getLabel()
        setLabel(oldLabel.copy(name = name))
    }

    fun setPush() {
        nextStep(InitEvent.Step.Push)
    }

    fun nextStep(from: InitEvent.Step) {
        if (from == InitEvent.Step.WatchAccount) {
            _eventFlow.tryEmit(InitEvent.Step.LabelAccount)
        } else if (from == InitEvent.Step.SelectAccount) {
            applyNameFromSelectedAccounts()
            _eventFlow.tryEmit(InitEvent.Step.LabelAccount)
        } else if (from == InitEvent.Step.Push) {
            execute()
        } else if (!hasPushPermission) {
            _eventFlow.tryEmit(InitEvent.Step.Push)
        } else {
            execute()
        }
    }

    private fun applyNameFromSelectedAccounts() {
        val selected = savedState.accounts?.filter { it.selected && !it.name.isNullOrBlank() } ?: return
        if (selected.isEmpty()) {
            return
        }
        val name = selected.first().name ?: return
        setLabelName(name)
    }

    private fun execute() {
        _eventFlow.tryEmit(InitEvent.Loading(true))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!passcodeRepository.hasPinCode) {
                    passcodeRepository.set(savedState.passcode!!)
                }

                if (type == InitArgs.Type.Watch) {
                    saveWatchWallet()
                } else if (type == InitArgs.Type.New) {
                    createNewWallet()
                } else if (type == InitArgs.Type.Import) {
                    importWallet()
                } else if (type == InitArgs.Type.Signer) {
                    signerWallet()
                }

                _eventFlow.tryEmit(InitEvent.Finish)
            } catch (e: Throwable) {
                _eventFlow.tryEmit(InitEvent.Loading(false))
            }
        }
    }

    private suspend fun createNewWallet() {
        walletRepository.createNewWallet(getLabel())
    }

    private suspend fun saveWatchWallet() {
        val account = getWatchAccount() ?: throw IllegalStateException("Account is not set")
        val label = getLabel()
        val publicKey = getPublicKey(account.address)

        walletRepository.addWatchWallet(publicKey, label, account.walletVersion, WalletSource.Default)
    }

    private suspend fun importWallet() {
        val versions = savedState.accounts?.filter { it.selected }?.map { it.walletVersion } ?: throw IllegalStateException("No selected accounts")
        val mnemonic = savedState.mnemonic ?: throw IllegalStateException("Mnemonic is not set")
        val seed = savedState.seed ?: throw IllegalStateException("Seed is not set")
        val label = getLabel()
        val privateKey = PrivateKeyEd25519(seed)
        val publicKey = privateKey.publicKey()

        walletRepository.addWallets(mnemonic, publicKey, versions, label.name, label.emoji, label.color, testnet)
    }

    private suspend fun signerWallet() {
        val label = getLabel()
        val publicKey = savedState.publicKey ?: throw IllegalStateException("Public key is not set")
        val walletSource = savedState.walletSource ?: throw IllegalStateException("Wallet source is not set")

        walletRepository.addSignerWallet(publicKey, label.name, label.emoji, label.color, walletSource)
    }

    private fun getPublicKey(
        accountId: String,
    ): PublicKeyEd25519 {
        api.getPublicKey(accountId, testnet).let { hex ->
            return PublicKeyEd25519(hex(hex))
        }
    }

}