package br.com.restful.cache;

import java.io.Serializable;

public class ProductObj implements Serializable
{
	private int ItemId;
	private String ProductThumb;
	private String ProductThumbBig;
	private String Title;
	private String Spec;
	private String Price;
	private String UsageDosage;
	private String ProductIntro;
	private String ProducterName;
	private String ProductCode;
	private int IsRx;
	
	private String GeneralName;
	private String BrandCode;
	private String otctype;
	
	public ProductObj() 
	{
		ItemId = 0;
		ProductThumb = "";
		ProductThumbBig = "";
		Title = "";
		Spec = "";
		Price = "";
		UsageDosage = "";
		ProductIntro = "";
		ProducterName = "";
		ProductCode = "";
		IsRx = 0;
		
		GeneralName = "";
		BrandCode = "";
		otctype = "";
	}
		
	public ProductObj(
					int item_id, String product_thumb, String product_thumb_big, String title
					, String spec, String price, String usgae_dosage, String product_intro
					, String producter_name, String product_code
					, int is_rx, String general_name, String brand_code, String otc_type
					) 
	{
		this.ItemId  = item_id;
		this.ProductThumb = product_thumb;
		this.ProductThumbBig = product_thumb_big;
		this.Title = title;
		this.Spec = spec;
		this.Price = price;
		this.UsageDosage = usgae_dosage;
		this.ProductIntro = product_intro;
		this.ProducterName = producter_name;
		this.ProductCode = product_code;
		this.IsRx = is_rx;
		
		this.GeneralName = general_name;
		this.BrandCode = brand_code;
		this.otctype = otc_type;
		
	}
	
	public int getItemId() 
	{
		return ItemId;
	}

	public void setItemId(int itemId) 
	{
		ItemId = itemId;
	}

	public String getProductThumb() 
	{
		return ProductThumb;
	}
	
	public void setProductThumb(String productThumb) 
	{
		ProductThumb = productThumb;
	}
	
	public String getTitle()
	{
		return Title;
	}
	
	public String getProductThumbBig() {
		return ProductThumbBig;
	}

	public void setProductThumbBig(String productThumbBig) {
		ProductThumbBig = productThumbBig;
	}
	
	public void setTitle(String title) 
	{
		Title = title;
	}
	
	public String getSpec() 
	{
		return Spec;
	}
	
	public void setSpec(String spec) 
	{
		Spec = spec;
	}
	public String getPrice()
	{
		return Price;
	}
	
	public void setPrice(String price)
	{
		Price = price;
	}
	public String getUsageDosage()
	{
		return UsageDosage;
	}
	
	public void setUsageDosage(String usageDosage)
	{
		UsageDosage = usageDosage;
	}
	
	public String getProductIntro() 
	{
		return ProductIntro;
	}
	
	public void setProductIntro(String productIntro)
	{
		ProductIntro = productIntro;
	}
	
	public String getProducterName() 
	{
		return ProducterName;
	}
	
	public void setProducterName(String producterName) 
	{
		ProducterName = producterName;
	}
	
	public String getProductCode() 
	{
		return ProductCode;
	}
	
	public void setProductCode(String productCode) 
	{
		ProductCode = productCode;
	}
	
	public int getIsRx()
	{
		return IsRx;
	}
	
	public void setIsRx(int isRx)
	{
		IsRx = isRx;
	}
	
	public String getGeneralName() 
	{
		return GeneralName;
	}

	public void setGeneralName(String generalName) 
	{
		GeneralName = generalName;
	}

	public String getBrandCode() 
	{
		return BrandCode;
	}

	public void setBrandCode(String brandCode) 
	{
		BrandCode = brandCode;
	}

	public String getOtctype()
	{
		if(IsRx == 0)
		{
			otctype = otctype.replace("otc", "OTC");
		}
		else{
			otctype = "Rx";
		}
		return otctype;
	}

	public void setOtctype(String otctype)
	{
		this.otctype = otctype;
	}
}
