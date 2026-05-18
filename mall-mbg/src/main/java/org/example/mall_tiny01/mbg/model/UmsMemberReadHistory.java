package org.example.mall_tiny01.mbg.model;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class UmsMemberReadHistory implements Serializable {
    private String id;
    private Long memberId;
    private String memberNickname;
    private String memberIcon;
    private Long productId;
    private String productName;
    private String productPic;
    private BigDecimal productPrice;
    private String productSubTitle;
    private Date createTime;
    private static final long serialVersionUID = 1L;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }
    public String getMemberIcon() { return memberIcon; }
    public void setMemberIcon(String memberIcon) { this.memberIcon = memberIcon; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductPic() { return productPic; }
    public void setProductPic(String productPic) { this.productPic = productPic; }
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
    public String getProductSubTitle() { return productSubTitle; }
    public void setProductSubTitle(String productSubTitle) { this.productSubTitle = productSubTitle; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}