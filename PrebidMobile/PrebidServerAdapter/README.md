A sample Open RTB 2.5 spec compliant request that Prebid Server Adapter would send out is as following:
```
{
	"id": "3d0c6321-9223-4baf-8875-1971c25ff3ce",
	"imp": [{
		"id": "Banner_300x250",
		"secure": 1,
		"ext": {
			"appnexus": {
				"placementId": 10433394
			}
		},
		"banner": {
			"format": [{
				"w": 300,
				"h": 250
			}, {
				"w": 300,
				"h": 600
			}]
		}
	}],
	"device": {
		"make": "unknown",
		"model": "Android SDK built for x86",
		"ua": "Mozilla\/5.0 (Linux; Android 6.0; Android SDK built for x86 Build\/MASTER; wv) AppleWebKit\/537.36 (KHTML, like Gecko) Version\/4.0 Chrome\/44.0.2403.119 Mobile Safari\/537.36",
		"w": 360,
		"h": 568,
		"pxratio": 3,
		"mccmnc": "310-260",
		"carrier": "Android",
		"connectiontype": 2,
		"lmt": 0,
		"ifa": "baa7acdc-b7de-48c2-91f7-d0343a74ae68",
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
		"keywords": "PrebidKeyword1;"
	},
	"user": {
		"yob": 1992,
		"gender": "F",
		"keywords": "PrebidKeyword2;"
	},
	"ext": {
		"prebid": {
			"targeting": {
				"pricegranularity": "medium",
				"lengthmax": 20
			},
			"source": "prebid-mobile",
			"version": "0.1.0"
		}
	}
}
```
