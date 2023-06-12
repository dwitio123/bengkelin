package com.rizky.bengkelin.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.maps.android.SphericalUtil
import com.rizky.bengkelin.data.remote.response.BengkelResult
import com.rizky.bengkelin.data.remote.response.CommonResponse
import com.rizky.bengkelin.data.repository.UserRepository
import com.rizky.bengkelin.ui.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun getBengkelList(
        userLoc: Location
    ): LiveData<Result<List<BengkelResult>>> = liveData {
        emit(Result.Loading)
        try {
            val response = userRepository.getBengkelList()
            if (response.isSuccessful) {
                response.body()?.let {
                    val sorted = sortBengkel(it.bengkelList, userLoc)
                    emit(Result.Success(sorted))
                } ?: run { emit(Result.Empty) }
            } else {
                val result = response.errorBody()?.string()
                val error: CommonResponse = GsonBuilder().create()
                    .fromJson(result, CommonResponse::class.java)
                emit(Result.Error(error.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    private fun sortBengkel(
        list: List<BengkelResult>,
        userLoc: Location
    ) = list.map { result ->
        val userLocation = LatLng(userLoc.latitude, userLoc.longitude)
        val bengkelLocation = LatLng(result.latitude, result.longitude)
        result.distance = SphericalUtil.computeDistanceBetween(
            userLocation, bengkelLocation
        ).toInt()
        result
    }.sortedBy { result -> result.distance }
}