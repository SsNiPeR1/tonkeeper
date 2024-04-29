package com.tonapps.tonkeeper.ui.screen.picker.list

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.emoji.ui.EmojiView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.data.account.WalletType
import uikit.extensions.drawable

abstract class Holder<I: Item>(
    parent: ViewGroup,
    @LayoutRes resId: Int,
    val onClick: (item: Item) -> Unit
): BaseListHolder<I>(parent, resId) {

    class Wallet(
        parent: ViewGroup,
        onClick: (item: Item) -> Unit
    ): Holder<Item.Wallet>(parent, R.layout.view_wallet_item, onClick) {

        private val colorView = findViewById<View>(R.id.wallet_color)
        private val emojiView = findViewById<EmojiView>(R.id.wallet_emoji)
        private val nameView = findViewById<AppCompatTextView>(R.id.wallet_name)
        private val balanceView = findViewById<AppCompatTextView>(R.id.wallet_balance)
        private val checkView = findViewById<AppCompatImageView>(R.id.check)
        private val typeView = findViewById<AppCompatTextView>(R.id.wallet_type)

        override fun onBind(item: Item.Wallet) {
            itemView.background = item.position.drawable(context)
            itemView.setOnClickListener { onClick(item) }

            colorView.backgroundTintList = ColorStateList.valueOf(item.color)
            emojiView.setEmoji(item.emoji)
            nameView.text = item.name
            balanceView.text = item.balance

            if (item.selected) {
                checkView.setImageResource(UIKitIcon.ic_donemark_otline_28)
            } else {
                checkView.setImageResource(0)
            }
            setType(item.walletType)
        }

        private fun setType(type: WalletType) {
            if (type == WalletType.Default) {
                typeView.visibility = View.GONE
                return
            }
            typeView.visibility = View.VISIBLE
            val resId = when (type) {
                WalletType.Watch -> Localization.watch_only
                WalletType.Testnet -> Localization.testnet
                WalletType.Signer -> Localization.signer
                else -> throw IllegalArgumentException("Unknown wallet type: $type")
            }
            typeView.setText(resId)
        }
    }

    class AddWallet(
        parent: ViewGroup,
        onClick: (item: Item) -> Unit
    ): Holder<Item.AddWallet>(parent, R.layout.view_wallet_add_item, onClick) {

        private val addButton = findViewById<View>(R.id.add)

        init {
            addButton.setOnClickListener { onClick(Item.AddWallet) }
        }

        override fun onBind(item: Item.AddWallet) {

        }
    }

}