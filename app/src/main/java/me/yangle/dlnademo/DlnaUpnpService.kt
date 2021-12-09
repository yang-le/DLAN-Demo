package me.yangle.dlnademo

import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl


class DlnaUpnpService : AndroidUpnpServiceImpl() {
    override fun createConfiguration(): UpnpServiceConfiguration {
        return object : AndroidUpnpServiceConfiguration() {
            public override fun createServiceDescriptorBinderUDA10(): ServiceDescriptorBinder {
                return UDA10ServiceDescriptorBinderImpl()
            }
        }
    }
}
