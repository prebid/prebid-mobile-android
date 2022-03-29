/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.video.vast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class InLine extends VASTParserBase
{
	private final static String VAST_INLINE = "InLine";
	private final static String VAST_ADSYSTEM = "AdSystem";
	private final static String VAST_ADTITLE = "AdTitle";
	private final static String VAST_DESCRIPTION = "Description";
	private final static String VAST_ADVERTISER = "Advertiser";
	private final static String VAST_PRICING = "Pricing";
	private final static String VAST_SURVEY = "Survey";
	private final static String VAST_ERROR = "Error";
	private final static String VAST_IMPRESSION = "Impression";
	private final static String VAST_CREATIVES = "Creatives";
	private final static String VAST_EXTENSIONS = "Extensions";
	private final static String VAST_AD_VERIFICATIONS = "AdVerifications";

	private AdSystem adSystem;
	private AdTitle adTitle;
	private Description description;
	private Advertiser advertiser;
	private Pricing pricing;
	private Survey survey;
	private Error error;
	private ArrayList<Impression> impressions;
	private ArrayList<Creative> creatives;
	private Extensions extensions;
	private AdVerifications adVerifications;

	public InLine(XmlPullParser p) throws XmlPullParserException, IOException {

		p.require(XmlPullParser.START_TAG, null, VAST_INLINE);

		while (p.next() != XmlPullParser.END_TAG) {
			if (p.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_ADSYSTEM))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADSYSTEM);
				adSystem = new AdSystem(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADSYSTEM);
			}
			else if (name != null && name.equals(VAST_ADTITLE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADTITLE);
				adTitle = new AdTitle(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADTITLE);
			}
			else if (name != null && name.equals(VAST_DESCRIPTION))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_DESCRIPTION);
				description = new Description(p);
				p.require(XmlPullParser.END_TAG, null, VAST_DESCRIPTION);
			}
			else if (name != null && name.equals(VAST_ADVERTISER))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADVERTISER);
				advertiser = new Advertiser(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADVERTISER);
			}
			else if (name != null && name.equals(VAST_PRICING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_PRICING);
				pricing = new Pricing(p);
				p.require(XmlPullParser.END_TAG, null, VAST_PRICING);
			}
			else if (name != null && name.equals(VAST_SURVEY))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_SURVEY);
				survey = new Survey(p);
				p.require(XmlPullParser.END_TAG, null, VAST_SURVEY);
			}
			else if (name != null && name.equals(VAST_ERROR))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ERROR);
				error = new Error(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ERROR);
			}
			else if (name != null && name.equals(VAST_IMPRESSION))
			{
				if (impressions == null) {
					impressions = new ArrayList<>();
				}
				p.require(XmlPullParser.START_TAG, null, VAST_IMPRESSION);
				impressions.add(new Impression(p));
				p.require(XmlPullParser.END_TAG, null, VAST_IMPRESSION);
			}
			else if (name != null && name.equals(VAST_CREATIVES))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_CREATIVES);
				creatives = (new Creatives(p)).getCreatives();
				p.require(XmlPullParser.END_TAG, null, VAST_CREATIVES);
			}
			else if (name != null && name.equals(VAST_EXTENSIONS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_EXTENSIONS);
				extensions = new Extensions(p);
				p.require(XmlPullParser.END_TAG, null, VAST_EXTENSIONS);
			}
			else if (name != null && name.equals(VAST_AD_VERIFICATIONS)) {
				p.require(XmlPullParser.START_TAG, null, VAST_AD_VERIFICATIONS);
				adVerifications = new AdVerifications(p);
				p.require(XmlPullParser.END_TAG, null, VAST_AD_VERIFICATIONS);
			}
			else
			{
				skip(p);
			}
		}

	}

	public AdSystem getAdSystem() {
		return adSystem;
	}

	public AdTitle getAdTitle() {
		return adTitle;
	}

	public Description getDescription() {
		return description;
	}

	public Advertiser getAdvertiser() {
		return advertiser;
	}

	public Pricing getPricing() {
		return pricing;
	}

	public Survey getSurvey() {
		return survey;
	}

	public Error getError() {
		return error;
	}

	public ArrayList<Impression> getImpressions() {
		return impressions;
	}

	public ArrayList<Creative> getCreatives() {
		return creatives;
	}

	public void setCreatives(ArrayList<Creative> creatives) {
		this.creatives = creatives;
	}

	public Extensions getExtensions() {
		return extensions;
	}

	public AdVerifications getAdVerifications() {
		return adVerifications;
	}
}
