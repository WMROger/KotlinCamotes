package com.example.kotlinactivities.adminPage.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel : ViewModel() {
    private val _categories = MutableLiveData<MutableList<String>>(mutableListOf("Deluxe Room", "Barkada Room", "Regular Room"))
    val categories: LiveData<MutableList<String>> get() = _categories

    fun addCategory(category: String) {
        _categories.value?.add(category)
        _categories.value = _categories.value // Notify observers
    }

    fun removeCategory(category: String) {
        _categories.value?.remove(category)
        _categories.value = _categories.value // Notify observers
    }
}
