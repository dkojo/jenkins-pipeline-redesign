namespace Catalog;

public record Product(string Sku, string Name, decimal Price);

public static class Products
{
    public static readonly IReadOnlyList<Product> All =
    [
        new("KB-01", "Keyboard", 49.99m),
        new("MN-02", "Monitor", 189.00m),
        new("HS-03", "Headset", 79.50m),
    ];

    public static Product? Find(string sku) => All.FirstOrDefault(p => p.Sku == sku);
}
