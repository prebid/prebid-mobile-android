#!/bin/bash

# Disable Web Proxy (HTTP) and Secure Web Proxy (HTTPS) after Appium proxy settings issue.

networks=("Wi-Fi" "Ethernet" "USB 10/100/1000 LAN")
for network in "${networks[@]}"; do

    webProxyState=$(networksetup -getwebproxy "$network" | awk {'print $2'} | awk 'FNR == 1 {print}' | cut -d' ' -f2)
    secureWebProxyState=$(networksetup -getsecurewebproxy "$network" | awk {'print $2'} | awk 'FNR == 1 {print}' | cut -d' ' -f2)

    # Web Proxy (HTTP)
    if [[ "$webProxyState" != "(null)" && "$webProxyState" == "Yes" ]]; then
        networksetup -setwebproxystate "$network" "Off"
        echo "$network"
        echo "Web Proxy (HTTP) Disabled"
    fi

    # Secure Web Proxy (HTTPS)
    if [[ "$secureWebProxyState" != "(null)" && "$secureWebProxyState" == "Yes" ]]; then
        networksetup -setsecurewebproxystate "$network" "Off"
        echo "$network"
        echo "Secure Web Proxy (HTTPS) Disabled"
    fi

done