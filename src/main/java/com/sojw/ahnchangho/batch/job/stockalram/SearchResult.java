package com.sojw.ahnchangho.batch.job.stockalram;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sojw.ahnchangho.core.type.BaseObject;

public class SearchResult extends BaseObject {
	@JsonProperty("err_code")
	private String errCode;
	@JsonProperty("err_msg")
	private String errMsg;
	@JsonProperty("page_no")
	private Integer pageNo;
	@JsonProperty("page_set")
	private Integer pageSet;
	@JsonProperty("total_count")
	private Integer totalCount;
	@JsonProperty("total_page")
	private Integer totalPage;
	@JsonProperty("list")
	private List<ListItem> list;

	public String getErrCode() {
		return errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public Integer getPageSet() {
		return pageSet;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public List<ListItem> getList() {
		return list;
	}

	public static class ListItem extends BaseObject {
		@JsonProperty("crp_cls")
		private String crpCls;
		@JsonProperty("crp_nm")
		private String crpNm;
		@JsonProperty("crp_cd")
		private String crpCd;
		@JsonProperty("rpt_nm")
		private String rptNm;
		@JsonProperty("rcp_no")
		private String rcpNo;
		@JsonProperty("flr_nm")
		private String flrNm;
		@JsonProperty("rcp_dt")
		private String rcpDt;
		@JsonProperty("rmk")
		private String rmk;

		public String getCrpCls() {
			return crpCls;
		}

		public String getCrpNm() {
			return crpNm;
		}

		public String getCrpCd() {
			return crpCd;
		}

		public String getRptNm() {
			return rptNm;
		}

		public String getRcpNo() {
			return rcpNo;
		}

		public String getFlrNm() {
			return flrNm;
		}

		public String getRcpDt() {
			return rcpDt;
		}

		public String getRmk() {
			return rmk;
		}

		public void setRptNm(String rptNm) {
			this.rptNm = rptNm;
		}
	}
}