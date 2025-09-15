package com.blueroots.carbonregistry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.blueroots.carbonregistry.data.models.CarbonCredit
import com.blueroots.carbonregistry.data.api.ApiClient

class CreditViewModel : ViewModel() {
    private val _credits = MutableLiveData<List<CarbonCredit>>()
    val credits: LiveData<List<CarbonCredit>> = _credits

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadUserCredits(userId: String = "demo-user") {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.apiService.getUserCredits(userId)

                if (response.isSuccessful && response.body() != null) {
                    _credits.value = response.body()!!
                } else {
                    _credits.value = emptyList()
                }
            } catch (e: Exception) {
                _credits.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}
