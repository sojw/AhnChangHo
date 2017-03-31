package com.sojw.ahnchangho.batch.job.pricesearch;

import java.util.List;

import com.sojw.ahnchangho.core.type.BaseObject;

public class PriceSearchResult extends BaseObject {
	public ProductCategoryFacetedSearch productCategoryFacetedSearch;

	public static class ProductCategoryFacetedSearch extends BaseObject {
		public ProductCategory productCategory;

		public static class ProductCategory extends BaseObject {
			public ProductCategoryPaginator productCategoryPaginator;
			public List<ChildProduct> childProducts;

			public static class ProductCategoryPaginator extends BaseObject {
				public String pageNumberRequested;
				public String pageNumberTotal;
			}

			public static class ChildProduct extends BaseObject {
				public String businessCatalogItemId;
				public String name;
				public QuicklookImage quicklookImage;
				public CategoryLargeImage categoryLargeImage;
				public String mupMessage;
				public Price price;

				public static class Price extends BaseObject {
					public String currentMaxPrice;
				}

				public static class QuicklookImage extends BaseObject {
					public String path;
				}

				public static class CategoryLargeImage extends BaseObject {
					public String path;
				}
			}
		}
	}

}