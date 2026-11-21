using Catalog;
using Xunit;

public class ProductsTests
{
    [Fact]
    public void ListsThreeProducts() => Assert.Equal(3, Products.All.Count);

    [Fact]
    public void FindsAProductBySku() => Assert.Equal("Monitor", Products.Find("MN-02")?.Name);

    [Fact]
    public void UnknownSkuIsNull() => Assert.Null(Products.Find("NOPE"));
}
