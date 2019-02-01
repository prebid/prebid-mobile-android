package org.prebid.mobile.testutils;

public class MockPrebidServerResponses {
    public static String noBid() {
        return "{\n" +
                "    \"id\": \"9f65eb71-37e7-4e9c-a212-7f8f443c648c\",\n" +
                "    \"ext\": {\n" +
                "        \"responsetimemillis\": {\n" +
                "            \"appnexus\": 2\n" +
                "        },\n" +
                "        \"tmaxrequest\": 500\n" +
                "    }\n" +
                "}";
    }

    public static String oneBidFromAppNexus() {
        return "{\n" +
                "  \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                "  \"seatbid\": [\n" +
                "    {\n" +
                "      \"bid\": [\n" +
                "        {\n" +
                "          \"id\": \"1644265211331914430\",\n" +
                "          \"impid\": \"Banner_300x250\",\n" +
                "          \"price\": 0.5,\n" +
                "          \"adm\": \"<script src=\\\"hello world\\\">this is an mock ad</script>\",\n" +
                "          \"adid\": \"113276871\",\n" +
                "          \"adomain\": [\n" +
                "            \"appnexus.com\"\n" +
                "          ],\n" +
                "          \"iurl\": \"https://nym1-ib.adnxs.com/cr?id=113276871\",\n" +
                "          \"cid\": \"9325\",\n" +
                "          \"crid\": \"113276871\",\n" +
                "          \"w\": 300,\n" +
                "          \"h\": 250,\n" +
                "          \"ext\": {\n" +
                "            \"prebid\": {\n" +
                "              \"targeting\": {\n" +
                "                \"hb_bidder\": \"appnexus\",\n" +
                "                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                "                \"hb_cache_id\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                "                \"hb_cache_id_appnexus\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                "                \"hb_creative_loadtype\": \"html\",\n" +
                "                \"hb_env\": \"mobile-app\",\n" +
                "                \"hb_env_appnexus\": \"mobile-app\",\n" +
                "                \"hb_pb\": \"0.50\",\n" +
                "                \"hb_pb_appnexus\": \"0.50\",\n" +
                "                \"hb_size\": \"300x250\",\n" +
                "                \"hb_size_appnexus\": \"300x250\"\n" +
                "              },\n" +
                "              \"type\": \"banner\"\n" +
                "            },\n" +
                "            \"bidder\": {\n" +
                "              \"appnexus\": {\n" +
                "                \"brand_id\": 1,\n" +
                "                \"auction_id\": 7888349588523321000,\n" +
                "                \"bidder_id\": 2,\n" +
                "                \"bid_ad_type\": 0\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"seat\": \"appnexus\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"ext\": {\n" +
                "    \"responsetimemillis\": {\n" +
                "      \"appnexus\": 213\n" +
                "    },\n" +
                "   \"tmaxrequest\":500\n" +
                "  }\n" +
                "}";
    }

    public static String oneBidFromAppNexusOneBidFromRubicon() {
        return "{\n" +
                "    \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                "    \"seatbid\": [\n" +
                "        {\n" +
                "            \"bid\": [\n" +
                "                {\n" +
                "                    \"id\": \"5805621923512124230\",\n" +
                "                    \"impid\": \"Banner_300x250\",\n" +
                "                    \"price\": 0.5,\n" +
                "                    \"adm\": \"<script src=\\\"https://ams1-ib.adnxs.com/ab?e=wqT_3QLiB6DiAwAAAwDWAAUBCPrAst8FEKmMlvLf1NnOURjYvJuGlrWf_gYqNgkAAAECCOA_EQEHNAAA4D8ZAAAAgOtR4D8hERIAKREJADERG6gw_bmsBjjtSEDtSEgCUMfvgTZYvv1cYABowNV2ePGPBYABAYoBA1VTRJIBAQb0YwGYAawCoAH6AagBAbABALgBAsABBMgBAtABAtgBGuABAPABAIoCPHVmKCdhJywgMjU4NDk5OSwgMTU0MjIzNDIzNCk7dWYoJ3InLCAxMTMyNzY4NzEsIDE1NDIyMzQyMzQpO5IC8QEhYXpWYWNnanVtOHdLRU1mdmdUWVlBQ0MtX1Z3d0FEZ0FRQVJJN1VoUV9ibXNCbGdBWU9BRmFBQndBSGdBZ0FFQWlBRUFrQUVCbUFFQm9BRUJxQUVEc0FFQXVRRXBpNGlEQUFEZ1A4RUJLWXVJZ3dBQTREX0pBZi0zZHFIYTd2d18yUUVBQUFBQUFBRHdQLUFCQVBVQkFBQUFBSmdDQUtBQ0FMVUNBQUFBQUwwQ0FBQUFBTUFDQU1nQ0FPQUNBT2dDQVBnQ05JQURCSkFEQUpnREFhZ0Q3cHZNQ3JvRENVRk5VekU2TkRJeU1lQUR5d0UumgJhIU5nN1dsUWp1bR30JHZ2MWNJQVFvQUQVkFhnUHpvSlFVMVRNVG8wTWpJeFFNc0JTUQGsGEFBQVBBX1URDAxBQUFXHQyI2AIA4AKymEjyAhMKD0NVU1RPTV9NT0RFTF9JRBIA8gIaChYyFgAgTEVBRl9OQU1FAR0IHgoaNh0ACEFTVAE-9AwBSUZJRUQSAIADAIgDAZADAJgDFKADAaoDAMADrALIAwDSAygIChIkYmFhN2FjZGMtYjdkZS00OGMyLTkxZjctZDAzNDNhNzRhZTY42AMA4AMA6AMC-AMAgAQAkgQJL29wZW5ydGIymAQAogQNMjA3LjIzNy4xNTAuMKgEArIEDAgAEAAYACAAMAA4ALgEAMAEAMgEANIEDjkzMjUjQU1TMTo0MjIx2gQCCAHgBADwBMfvgTaCBRlvcmcucHJlYmlkLm1vYmlsZS5kZW1vYXBwiAUBmAUAoAX___________8BqgUkM2RjNzY2NjctYTUwMC00ZTAxLWE0M2ItMzY4ZTM2ZDZjN2NjwAUAyQVpXRTwP9IFCQkJDDQAANgFAeAFAfAFAfoFBAGuVJAGAJgGALgGAMEGAAAAAAAA8D_IBgA.&s=502a4c2b37f28e4dd93d582739010543f9da4764&pp=${AUCTION_PRICE}\\\"></script>\",\n" +
                "                    \"adid\": \"113276871\",\n" +
                "                    \"adomain\": [\n" +
                "                        \"appnexus.com\"\n" +
                "                    ],\n" +
                "                    \"iurl\": \"https://ams1-ib.adnxs.com/cr?id=113276871\",\n" +
                "                    \"cid\": \"9325\",\n" +
                "                    \"crid\": \"113276871\",\n" +
                "                    \"w\": 300,\n" +
                "                    \"h\": 250,\n" +
                "                    \"ext\": {\n" +
                "                        \"prebid\": {\n" +
                "                            \"targeting\": {\n" +
                "                                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                "                                \"hb_cache_id_appnexus\": \"f5b7ff9f-4311-459d-a5ac-5d4d3d034e47\",\n" +
                "                                \"hb_creative_loadtype\": \"html\",\n" +
                "                                \"hb_env_appnexus\": \"mobile-app\",\n" +
                "                                \"hb_pb_appnexus\": \"0.50\",\n" +
                "                                \"hb_size_appnexus\": \"300x250\"\n" +
                "                            },\n" +
                "                            \"type\": \"banner\"\n" +
                "                        },\n" +
                "                        \"bidder\": {\n" +
                "                            \"appnexus\": {\n" +
                "                                \"brand_id\": 1,\n" +
                "                                \"auction_id\": 5880969551537341993,\n" +
                "                                \"bidder_id\": 2,\n" +
                "                                \"bid_ad_type\": 0\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"seat\": \"appnexus\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"bid\": [\n" +
                "                {\n" +
                "                    \"id\": \"0\",\n" +
                "                    \"impid\": \"Banner_300x250\",\n" +
                "                    \"price\": 1.23,\n" +
                "                    \"adm\": \"    <script type=\\\"text/javascript\\\">\\n      rubicon_cb = Math.random(); rubicon_rurl = document.referrer; if(top.location==document.location){rubicon_rurl = document.location;} rubicon_rurl = escape(rubicon_rurl);\\n      window.rubicon_ad = \\\"4073548\\\" + \\\".\\\" + \\\"js\\\";\\n      window.rubicon_creative = \\\"4458534\\\" + \\\".\\\" + \\\"js\\\";\\n    </script>\\n<div data-rp-type=\\\"trp-display-creative\\\" data-rp-impression-id=\\\"32a8ff34-535e-47ce-ac8b-e2fc52ca4b08\\\" data-rp-aqid=\\\"0:\\\" data-rp-acct-id=\\\"1001\\\">\\n<div style=\\\"width: 0; height: 0; overflow: hidden;\\\"><img border=\\\"0\\\" width=\\\"1\\\" height=\\\"1\\\" src=\\\"https://beacon-eu-ams3.rubiconproject.com/beacon/d/32a8ff34-535e-47ce-ac8b-e2fc52ca4b08?oo=0&accountId=1001&siteId=113932&zoneId=535510&sizeId=15&e=6A1E40E384DA563B775C5139AC1BE9F8CB0852C670DA88D15686E8B099C3849C4A8A1605B8120BE753D707300BDA19D4683D50851BF2EEDFA292263C8F70275E30C1E12B3D51E6B24242CC624DE62CD4BB342D372FA824977125290B6EF4C671988040E7A3416533D62E938E0170F9EC091BC0B8A9EE677F0D0FC5720B6414D79F3AAF0C74326D84E82A954C1004678A\\\" alt=\\\"\\\" /></div>\\n\\n<a href=\\\"http://optimized-by.rubiconproject.com/t/1001/113932/535510-15.4073548.4458534?url=http%3A%2F%2Frubiconproject.com\\\" target=\\\"_blank\\\"><img src=\\\"https://secure-assets.rubiconproject.com/campaigns/1001/50/59/48/1476242257campaign_file_q06ab2.png\\\" border=\\\"0\\\" alt=\\\"\\\" /></a>\\n<div style=\\\"height:0px;width:0px;overflow:hidden\\\"><iframe src=\\\"https://eus.rubiconproject.com/usync.html?&geo=na&co=us\\\" frameborder=\\\"0\\\" marginwidth=\\\"0\\\" marginheight=\\\"0\\\" scrolling=\\\"NO\\\" width=\\\"0\\\" height=\\\"0\\\" style=\\\"height:0px;width:0px\\\"></iframe></div></div>\\n\\n\",\n" +
                "                    \"crid\": \"4458534\",\n" +
                "                    \"w\": 300,\n" +
                "                    \"h\": 250,\n" +
                "                    \"ext\": {\n" +
                "                        \"prebid\": {\n" +
                "                            \"targeting\": {\n" +
                "                                \"hb_bidder\": \"rubicon\",\n" +
                "                                \"hb_bidder_rubicon\": \"rubicon\",\n" +
                "                                \"hb_cache_id\": \"bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7\",\n" +
                "                                \"hb_cache_id_rubicon\": \"bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7\",\n" +
                "                                \"hb_creative_loadtype\": \"html\",\n" +
                "                                \"hb_env\": \"mobile-app\",\n" +
                "                                \"hb_env_rubicon\": \"mobile-app\",\n" +
                "                                \"hb_pb\": \"1.20\",\n" +
                "                                \"hb_pb_rubicon\": \"1.20\",\n" +
                "                                \"hb_size\": \"300x250\",\n" +
                "                                \"hb_size_rubicon\": \"300x250\"\n" +
                "                            },\n" +
                "                            \"type\": \"banner\"\n" +
                "                        },\n" +
                "                        \"bidder\": {\n" +
                "                            \"rp\": {\n" +
                "                                \"targeting\": [\n" +
                "                                    {\n" +
                "                                        \"key\": \"rpfl_1001\",\n" +
                "                                        \"values\": [\n" +
                "                                            \"15_tier0100\"\n" +
                "                                        ]\n" +
                "                                    }\n" +
                "                                ],\n" +
                "                                \"mime\": \"text/html\",\n" +
                "                                \"size_id\": 15\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"seat\": \"rubicon\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"ext\": {\n" +
                "        \"responsetimemillis\": {\n" +
                "            \"appnexus\": 76,\n" +
                "            \"rubicon\": 4\n" +
                "    },\n" +
                "   \"tmaxrequest\":500\n" +
                "    }\n" +
                "}";
    }

    public static String invalidBidResponseWithoutCacheId() {
        return "{\n" +
                "  \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                "  \"seatbid\": [\n" +
                "    {\n" +
                "      \"bid\": [\n" +
                "        {\n" +
                "          \"id\": \"1644265211331914430\",\n" +
                "          \"impid\": \"Banner_300x250\",\n" +
                "          \"price\": 0.5,\n" +
                "          \"adm\": \"<script src=\\\"hello world\\\">this is an mock ad</script>\",\n" +
                "          \"adid\": \"113276871\",\n" +
                "          \"adomain\": [\n" +
                "            \"appnexus.com\"\n" +
                "          ],\n" +
                "          \"iurl\": \"https://nym1-ib.adnxs.com/cr?id=113276871\",\n" +
                "          \"cid\": \"9325\",\n" +
                "          \"crid\": \"113276871\",\n" +
                "          \"w\": 300,\n" +
                "          \"h\": 250,\n" +
                "          \"ext\": {\n" +
                "            \"prebid\": {\n" +
                "              \"targeting\": {\n" +
                "                \"hb_bidder\": \"appnexus\",\n" +
                "                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                "                \"hb_creative_loadtype\": \"html\",\n" +
                "                \"hb_env\": \"mobile-app\",\n" +
                "                \"hb_env_appnexus\": \"mobile-app\",\n" +
                "                \"hb_pb\": \"0.50\",\n" +
                "                \"hb_pb_appnexus\": \"0.50\",\n" +
                "                \"hb_size\": \"300x250\",\n" +
                "                \"hb_size_appnexus\": \"300x250\"\n" +
                "              },\n" +
                "              \"type\": \"banner\"\n" +
                "            },\n" +
                "            \"bidder\": {\n" +
                "              \"appnexus\": {\n" +
                "                \"brand_id\": 1,\n" +
                "                \"auction_id\": 7888349588523321000,\n" +
                "                \"bidder_id\": 2,\n" +
                "                \"bid_ad_type\": 0\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"seat\": \"appnexus\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"ext\": {\n" +
                "    \"responsetimemillis\": {\n" +
                "      \"appnexus\": 213\n" +
                "    },\n" +
                "   \"tmaxrequest\":500\n" +
                "  }\n" +
                "}";
    }

    public static String validResponseAppNexusNoCacheIdAndRubiconHasCacheId() {
        return "{\n" +
                "    \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                "    \"seatbid\": [\n" +
                "        {\n" +
                "            \"bid\": [\n" +
                "                {\n" +
                "                    \"id\": \"5805621923512124230\",\n" +
                "                    \"impid\": \"Banner_300x250\",\n" +
                "                    \"price\": 0.5,\n" +
                "                    \"adm\": \"<script src=\\\"https://ams1-ib.adnxs.com/ab?e=wqT_3QLiB6DiAwAAAwDWAAUBCPrAst8FEKmMlvLf1NnOURjYvJuGlrWf_gYqNgkAAAECCOA_EQEHNAAA4D8ZAAAAgOtR4D8hERIAKREJADERG6gw_bmsBjjtSEDtSEgCUMfvgTZYvv1cYABowNV2ePGPBYABAYoBA1VTRJIBAQb0YwGYAawCoAH6AagBAbABALgBAsABBMgBAtABAtgBGuABAPABAIoCPHVmKCdhJywgMjU4NDk5OSwgMTU0MjIzNDIzNCk7dWYoJ3InLCAxMTMyNzY4NzEsIDE1NDIyMzQyMzQpO5IC8QEhYXpWYWNnanVtOHdLRU1mdmdUWVlBQ0MtX1Z3d0FEZ0FRQVJJN1VoUV9ibXNCbGdBWU9BRmFBQndBSGdBZ0FFQWlBRUFrQUVCbUFFQm9BRUJxQUVEc0FFQXVRRXBpNGlEQUFEZ1A4RUJLWXVJZ3dBQTREX0pBZi0zZHFIYTd2d18yUUVBQUFBQUFBRHdQLUFCQVBVQkFBQUFBSmdDQUtBQ0FMVUNBQUFBQUwwQ0FBQUFBTUFDQU1nQ0FPQUNBT2dDQVBnQ05JQURCSkFEQUpnREFhZ0Q3cHZNQ3JvRENVRk5VekU2TkRJeU1lQUR5d0UumgJhIU5nN1dsUWp1bR30JHZ2MWNJQVFvQUQVkFhnUHpvSlFVMVRNVG8wTWpJeFFNc0JTUQGsGEFBQVBBX1URDAxBQUFXHQyI2AIA4AKymEjyAhMKD0NVU1RPTV9NT0RFTF9JRBIA8gIaChYyFgAgTEVBRl9OQU1FAR0IHgoaNh0ACEFTVAE-9AwBSUZJRUQSAIADAIgDAZADAJgDFKADAaoDAMADrALIAwDSAygIChIkYmFhN2FjZGMtYjdkZS00OGMyLTkxZjctZDAzNDNhNzRhZTY42AMA4AMA6AMC-AMAgAQAkgQJL29wZW5ydGIymAQAogQNMjA3LjIzNy4xNTAuMKgEArIEDAgAEAAYACAAMAA4ALgEAMAEAMgEANIEDjkzMjUjQU1TMTo0MjIx2gQCCAHgBADwBMfvgTaCBRlvcmcucHJlYmlkLm1vYmlsZS5kZW1vYXBwiAUBmAUAoAX___________8BqgUkM2RjNzY2NjctYTUwMC00ZTAxLWE0M2ItMzY4ZTM2ZDZjN2NjwAUAyQVpXRTwP9IFCQkJDDQAANgFAeAFAfAFAfoFBAGuVJAGAJgGALgGAMEGAAAAAAAA8D_IBgA.&s=502a4c2b37f28e4dd93d582739010543f9da4764&pp=${AUCTION_PRICE}\\\"></script>\",\n" +
                "                    \"adid\": \"113276871\",\n" +
                "                    \"adomain\": [\n" +
                "                        \"appnexus.com\"\n" +
                "                    ],\n" +
                "                    \"iurl\": \"https://ams1-ib.adnxs.com/cr?id=113276871\",\n" +
                "                    \"cid\": \"9325\",\n" +
                "                    \"crid\": \"113276871\",\n" +
                "                    \"w\": 300,\n" +
                "                    \"h\": 250,\n" +
                "                    \"ext\": {\n" +
                "                        \"prebid\": {\n" +
                "                            \"targeting\": {\n" +
                "                                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                "                                \"hb_creative_loadtype\": \"html\",\n" +
                "                                \"hb_env_appnexus\": \"mobile-app\",\n" +
                "                                \"hb_pb_appnexus\": \"0.50\",\n" +
                "                                \"hb_size_appnexus\": \"300x250\"\n" +
                "                            },\n" +
                "                            \"type\": \"banner\"\n" +
                "                        },\n" +
                "                        \"bidder\": {\n" +
                "                            \"appnexus\": {\n" +
                "                                \"brand_id\": 1,\n" +
                "                                \"auction_id\": 5880969551537341993,\n" +
                "                                \"bidder_id\": 2,\n" +
                "                                \"bid_ad_type\": 0\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"seat\": \"appnexus\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"bid\": [\n" +
                "                {\n" +
                "                    \"id\": \"0\",\n" +
                "                    \"impid\": \"Banner_300x250\",\n" +
                "                    \"price\": 1.23,\n" +
                "                    \"adm\": \"    <script type=\\\"text/javascript\\\">\\n      rubicon_cb = Math.random(); rubicon_rurl = document.referrer; if(top.location==document.location){rubicon_rurl = document.location;} rubicon_rurl = escape(rubicon_rurl);\\n      window.rubicon_ad = \\\"4073548\\\" + \\\".\\\" + \\\"js\\\";\\n      window.rubicon_creative = \\\"4458534\\\" + \\\".\\\" + \\\"js\\\";\\n    </script>\\n<div data-rp-type=\\\"trp-display-creative\\\" data-rp-impression-id=\\\"32a8ff34-535e-47ce-ac8b-e2fc52ca4b08\\\" data-rp-aqid=\\\"0:\\\" data-rp-acct-id=\\\"1001\\\">\\n<div style=\\\"width: 0; height: 0; overflow: hidden;\\\"><img border=\\\"0\\\" width=\\\"1\\\" height=\\\"1\\\" src=\\\"https://beacon-eu-ams3.rubiconproject.com/beacon/d/32a8ff34-535e-47ce-ac8b-e2fc52ca4b08?oo=0&accountId=1001&siteId=113932&zoneId=535510&sizeId=15&e=6A1E40E384DA563B775C5139AC1BE9F8CB0852C670DA88D15686E8B099C3849C4A8A1605B8120BE753D707300BDA19D4683D50851BF2EEDFA292263C8F70275E30C1E12B3D51E6B24242CC624DE62CD4BB342D372FA824977125290B6EF4C671988040E7A3416533D62E938E0170F9EC091BC0B8A9EE677F0D0FC5720B6414D79F3AAF0C74326D84E82A954C1004678A\\\" alt=\\\"\\\" /></div>\\n\\n<a href=\\\"http://optimized-by.rubiconproject.com/t/1001/113932/535510-15.4073548.4458534?url=http%3A%2F%2Frubiconproject.com\\\" target=\\\"_blank\\\"><img src=\\\"https://secure-assets.rubiconproject.com/campaigns/1001/50/59/48/1476242257campaign_file_q06ab2.png\\\" border=\\\"0\\\" alt=\\\"\\\" /></a>\\n<div style=\\\"height:0px;width:0px;overflow:hidden\\\"><iframe src=\\\"https://eus.rubiconproject.com/usync.html?&geo=na&co=us\\\" frameborder=\\\"0\\\" marginwidth=\\\"0\\\" marginheight=\\\"0\\\" scrolling=\\\"NO\\\" width=\\\"0\\\" height=\\\"0\\\" style=\\\"height:0px;width:0px\\\"></iframe></div></div>\\n\\n\",\n" +
                "                    \"crid\": \"4458534\",\n" +
                "                    \"w\": 300,\n" +
                "                    \"h\": 250,\n" +
                "                    \"ext\": {\n" +
                "                        \"prebid\": {\n" +
                "                            \"targeting\": {\n" +
                "                                \"hb_bidder\": \"rubicon\",\n" +
                "                                \"hb_bidder_rubicon\": \"rubicon\",\n" +
                "                                \"hb_cache_id\": \"bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7\",\n" +
                "                                \"hb_cache_id_rubicon\": \"bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7\",\n" +
                "                                \"hb_creative_loadtype\": \"html\",\n" +
                "                                \"hb_env\": \"mobile-app\",\n" +
                "                                \"hb_env_rubicon\": \"mobile-app\",\n" +
                "                                \"hb_pb\": \"1.20\",\n" +
                "                                \"hb_pb_rubicon\": \"1.20\",\n" +
                "                                \"hb_size\": \"300x250\",\n" +
                "                                \"hb_size_rubicon\": \"300x250\"\n" +
                "                            },\n" +
                "                            \"type\": \"banner\"\n" +
                "                        },\n" +
                "                        \"bidder\": {\n" +
                "                            \"rp\": {\n" +
                "                                \"targeting\": [\n" +
                "                                    {\n" +
                "                                        \"key\": \"rpfl_1001\",\n" +
                "                                        \"values\": [\n" +
                "                                            \"15_tier0100\"\n" +
                "                                        ]\n" +
                "                                    }\n" +
                "                                ],\n" +
                "                                \"mime\": \"text/html\",\n" +
                "                                \"size_id\": 15\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"seat\": \"rubicon\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"ext\": {\n" +
                "        \"responsetimemillis\": {\n" +
                "            \"appnexus\": 76,\n" +
                "            \"rubicon\": 4\n" +
                "    },\n" +
                "   \"tmaxrequest\":500\n" +
                "    }\n" +
                "}";
    }
}
