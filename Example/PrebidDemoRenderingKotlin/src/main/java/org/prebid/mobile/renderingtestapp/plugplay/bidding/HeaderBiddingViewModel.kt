package org.prebid.mobile.renderingtestapp.plugplay.bidding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.prebid.mobile.renderingtestapp.data.DemoItem
import org.prebid.mobile.renderingtestapp.data.Tag
import org.prebid.mobile.renderingtestapp.utils.ConfigurationViewSettings
import org.prebid.mobile.renderingtestapp.utils.DemoItemProvider
import org.prebid.mobile.renderingtestapp.utils.SourcePicker

class HeaderBiddingViewModel(
        private val integrationCategories: Array<String>,
        private val adCategories: Array<String>
) : ViewModel() {

    private var demoItemList: MutableList<DemoItem> = DemoItemProvider.getDemoList()

    private var integrationSelectedPosition = 0
    private var adCategorySelectedPosition = 0
    private var searchQuery: String = ""

    private val _demoItems = MutableLiveData<List<DemoItem>>()
    val demoItems: LiveData<List<DemoItem>>
        get() = _demoItems

    private val _navigateToDemoExample = MutableLiveData<DemoItem>()
    val navigateToDemoExample: LiveData<DemoItem>
        get() = _navigateToDemoExample

    private val _configurationState = MutableLiveData<Boolean>()
    val configurationState: LiveData<Boolean>
        get() = _configurationState

    init {
        _configurationState.value = ConfigurationViewSettings.isEnabled
        integrationSelectedPosition = integrationCategories.size - 1
        adCategorySelectedPosition = adCategories.size - 1
        updateDemoList()
    }

    fun getIntegrationCategoriesPosition() = integrationSelectedPosition

    fun getAdCategoriesPosition() = adCategorySelectedPosition

    fun onSearchTextChanged(newQuery: String?) {
        if (newQuery != null) {
            searchQuery = newQuery.toLowerCase()
            updateDemoList()
        }
    }

    fun onIntegrationControlChanged(position: Int) {
        integrationSelectedPosition = position
        updateDemoList()
    }

    fun onAdCategoryControlChanged(position: Int) {
        adCategorySelectedPosition = position
        updateDemoList()
    }

    fun isMockServer(): Boolean {
        return SourcePicker.useMockServer
    }

    fun onMockSwitchStateChanged(isChecked: Boolean) {
        SourcePicker.useMockServer = isChecked
        updateDemoList()
    }

    fun onDemoItemClicked(item: DemoItem) {
        _navigateToDemoExample.value = item
    }

    fun onDemoItemNavigated() {
        _navigateToDemoExample.value = null
    }

    fun onConfigurationToggleChanged(isChecked: Boolean) {
        if (isChecked != configurationState.value) {
            ConfigurationViewSettings.isEnabled = isChecked
            _configurationState.value = isChecked
        }
    }

    private fun updateDemoList() {
        _demoItems.value = demoItemList.filter { demoItem ->
            containsProperRemoteTag(demoItem.tag)
                    && containsProperIntegrationTag(demoItem.tag)
                    && containsProperAdTag(demoItem.tag)
                    && containsSearchQuery(demoItem.label)
        }
    }

    private fun containsProperRemoteTag(tags: List<Tag>): Boolean {
        val requiredTag = if (isMockServer()) Tag.MOCK else Tag.REMOTE
        return tags.contains(requiredTag)
    }

    private fun containsProperIntegrationTag(tags: List<Tag>): Boolean {
        return containsCategoryTag(tags, integrationCategories[integrationSelectedPosition])
    }

    private fun containsProperAdTag(tags: List<Tag>): Boolean {
        return containsCategoryTag(tags, adCategories[adCategorySelectedPosition])
    }

    private fun containsCategoryTag(tags: List<Tag>, requiredTag: String): Boolean {
        if (Tag.ALL.tagName == requiredTag) {
            return true
        }
        val tagMap = tags.map { it.tagName }
        return tagMap.contains(requiredTag)
    }

    private fun containsSearchQuery(data: String): Boolean {
        return data.toLowerCase().contains(searchQuery)
    }
}