package ca.burchill.cointracker.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ca.burchill.cointracker.database.CoinsDatabase
import ca.burchill.cointracker.database.asDomainModel
import ca.burchill.cointracker.domain.Coin
import ca.burchill.cointracker.network.CoinApi
import ca.burchill.cointracker.network.NetworkCoin
import ca.burchill.cointracker.network.NetworkCoinContainer
import ca.burchill.cointracker.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class CoinsRepository (private val database: CoinsDatabase) {

    val coins: LiveData<List<Coin>> = Transformations.map(database.coinDao.getCoins()) {
        it.asDomainModel()
    }

    /**
     * Refresh the coins stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    suspend fun refreshCoins() {
        withContext(Dispatchers.IO) {
            Timber.d("refresh coins was called")
            val coinList = NetworkCoinContainer(CoinApi.retrofitService.getCoins().status,CoinApi.retrofitService.getCoins().coins)
            database.coinDao.insertAll(coinList.asDatabaseModel())
        }
    }
}