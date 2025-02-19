package com.tonapps.tonkeeper.ui.screen.staking.viewer.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.staking.viewer.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.staking.StakingPool
import uikit.widget.FrescoView

class BalanceHolder(
    parent: ViewGroup,
): Holder<Item.Balance>(parent, R.layout.view_staking_balance) {

    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val fiatView = findViewById<AppCompatTextView>(R.id.fiat)
    private val iconView = findViewById<FrescoView>(R.id.icon)

    override fun onBind(item: Item.Balance) {
        balanceView.text = item.balanceFormat
        fiatView.text = item.fiatFormat
        iconView.setLocalRes(StakingPool.getIcon(item.poolImplementation))
    }

}