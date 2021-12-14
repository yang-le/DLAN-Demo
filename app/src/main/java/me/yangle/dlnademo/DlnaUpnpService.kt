package me.yangle.dlnademo

import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl


class DlnaUpnpService : AndroidUpnpServiceImpl() {
    override fun createConfiguration() = object : AndroidUpnpServiceConfiguration() {
        override fun createServiceDescriptorBinderUDA10() = UDA10ServiceDescriptorBinderImpl()
    }
}
