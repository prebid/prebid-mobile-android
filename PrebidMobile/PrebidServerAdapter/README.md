A sample Open RTB 2.5 spec compliant request that Prebid Server Adapter would send out is as following:
```
{
  "id": "1a31e229-9e6f-4a4b-87be-8a19a650bd9b",
  "source": {
    "tid": "123"
  },
  "imp": [
    {
      "id": "Banner_320x50",
      "secure": 1,
      "ext": {
        "prebid": {
          "storedrequest": {
            "id": "eebc307d-7f76-45d6-a7a7-68985169b138"
          }
        }
      },
      "banner":{
        "format":[
          {
            "w": 300,
            "h": 250
          }
        ]
      }
    }
  ],
  "device": {
    "make": "unknown",
    "model": "Android SDK built for x86",
    "w": 360,
    "h": 568,
    "pxratio": 3,
    "mccmnc": "310-260",
    "carrier": "Android",
    "connectiontype": 2,
    "lmt": 0,
    "os": "android",
    "osv": "23",
    "language": "en"
  },
  "app": {
    "bundle": "org.prebid.mobile.demoapp",
    "ver": "0.1.0",
    "name": "DemoApp",
    "privacypolicy": 0,
    "publisher": {
      "id": "bfa84af2-bd16-4d35-96ad-31c6bb888df0"
    },
    "keywords": "PrebidKeyword1,",
    "ext": {
      "prebid": {
        "source": "prebid-mobile",
        "version": "0.1.0"
      }
    }
  },
  "user": {
    "yob": 1992,
    "gender": "F",
    "keywords": "PrebidKeyword2,"
  },
  "ext": {
    "prebid": {
      "targeting": {
        "pricegranularity": "medium",
        "lengthmax": 20
      }
    }
  }
}
```
