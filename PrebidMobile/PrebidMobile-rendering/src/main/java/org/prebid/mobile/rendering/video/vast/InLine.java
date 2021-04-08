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

	private AdSystem mAdSystem;
	private AdTitle mAdTitle;
	private Description mDescription;
	private Advertiser mAdvertiser;
	private Pricing mPricing;
	private Survey mSurvey;
	private Error mError;
	private ArrayList<Impression> mImpressions;
	private ArrayList<Creative> mCreatives;
	private Extensions mExtensions;
	private AdVerifications mAdVerifications;

	public InLine(XmlPullParser p) throws XmlPullParserException, IOException
	{

		p.require(XmlPullParser.START_TAG, null, VAST_INLINE);

		while (p.next() != XmlPullParser.END_TAG)
		{
			if (p.getEventType() != XmlPullParser.START_TAG)
			{
				continue;
			}
			String name = p.getName();
			if (name != null && name.equals(VAST_ADSYSTEM))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADSYSTEM);
				mAdSystem = new AdSystem(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADSYSTEM);
			}
			else if (name != null && name.equals(VAST_ADTITLE))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADTITLE);
				mAdTitle = new AdTitle(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADTITLE);
			}
			else if (name != null && name.equals(VAST_DESCRIPTION))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_DESCRIPTION);
				mDescription = new Description(p);
				p.require(XmlPullParser.END_TAG, null, VAST_DESCRIPTION);
			}
			else if (name != null && name.equals(VAST_ADVERTISER))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ADVERTISER);
				mAdvertiser = new Advertiser(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ADVERTISER);
			}
			else if (name != null && name.equals(VAST_PRICING))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_PRICING);
				mPricing = new Pricing(p);
				p.require(XmlPullParser.END_TAG, null, VAST_PRICING);
			}
			else if (name != null && name.equals(VAST_SURVEY))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_SURVEY);
				mSurvey = new Survey(p);
				p.require(XmlPullParser.END_TAG, null, VAST_SURVEY);
			}
			else if (name != null && name.equals(VAST_ERROR))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_ERROR);
				mError = new Error(p);
				p.require(XmlPullParser.END_TAG, null, VAST_ERROR);
			}
			else if (name != null && name.equals(VAST_IMPRESSION))
			{
				if (mImpressions == null)
				{
					mImpressions = new ArrayList<>();
				}
				p.require(XmlPullParser.START_TAG, null, VAST_IMPRESSION);
				mImpressions.add(new Impression(p));
				p.require(XmlPullParser.END_TAG, null, VAST_IMPRESSION);
			}
			else if (name != null && name.equals(VAST_CREATIVES))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_CREATIVES);
				mCreatives = (new Creatives(p)).getCreatives();
				p.require(XmlPullParser.END_TAG, null, VAST_CREATIVES);
			}
			else if (name != null && name.equals(VAST_EXTENSIONS))
			{
				p.require(XmlPullParser.START_TAG, null, VAST_EXTENSIONS);
				mExtensions = new Extensions(p);
				p.require(XmlPullParser.END_TAG, null, VAST_EXTENSIONS);
			}
			else if (name != null && name.equals(VAST_AD_VERIFICATIONS)) {
				p.require(XmlPullParser.START_TAG, null, VAST_AD_VERIFICATIONS);
				mAdVerifications = new AdVerifications(p);
				p.require(XmlPullParser.END_TAG, null, VAST_AD_VERIFICATIONS);
			}
			else
			{
				skip(p);
			}
		}

	}

	public AdSystem getAdSystem() {
		return mAdSystem;
	}

	public AdTitle getAdTitle() {
		return mAdTitle;
	}

	public Description getDescription() {
		return mDescription;
	}

	public Advertiser getAdvertiser() {
		return mAdvertiser;
	}

	public Pricing getPricing() {
		return mPricing;
	}

	public Survey getSurvey() {
		return mSurvey;
	}

	public Error getError() {
		return mError;
	}

	public ArrayList<Impression> getImpressions() {
		return mImpressions;
	}

	public ArrayList<Creative> getCreatives() {
		return mCreatives;
	}

	public void setCreatives(ArrayList<Creative> creatives) {
		mCreatives = creatives;
	}

	public Extensions getExtensions() {
		return mExtensions;
	}

	public AdVerifications getAdVerifications() {
		return mAdVerifications;
	}
}
