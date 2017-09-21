/**
 * 
 */
package com.sojw.ahnchangho;

import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

/**
 * @author Naver
 *
 */
public class DividendGrabTest {

	private static final String COMPANY_INFO = "http://companyinfo.stock.naver.com/v1/company/ajax/cF1001.aspx?fin_typ=0&freq_typ=Q&cmp_cd=";

	/**
	 * Test method for {@link com.sojw.ahnchangho.DividendGrab#test()}.
	 */
	@Test
	public void testTest() throws Exception {
		Document companyInfoDocument = null;
		try {
			companyInfoDocument = Jsoup.connect(COMPANY_INFO + "085620").get();
		} catch (IOException e) {
			System.out.println(e);
		}

		final Elements benefitElements = companyInfoDocument.select("table > tbody > tr:nth-child(2) > td:not(.bgE)");
		if (benefitElements != null) {
			final Integer firstBenefit = NumberUtils.toInt(benefitElements.first().text());
			System.out.println(firstBenefit);

			final Integer lastBenefit = NumberUtils.toInt(benefitElements.last().text());
			System.out.println(lastBenefit);
		}

		final Elements debtRateElements = companyInfoDocument.select("table > tbody > tr:nth-child(23) > td:not(.bgE)");
		System.out.println(debtRateElements);

		final Elements retentionRateElements = companyInfoDocument.select("table > tbody > tr:nth-child(24) > td:not(.bgE)");
		System.out.println(retentionRateElements);
	}

}
