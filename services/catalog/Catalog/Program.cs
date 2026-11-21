// catalog: the .NET microservice. Serves the product list behind NGINX at /catalog/.
using Catalog;

var app = WebApplication.CreateBuilder(args).Build();
app.MapGet("/catalog/health", () => Results.Text("ok"));
app.MapGet("/catalog/products", () => Results.Json(new { products = Products.All, count = Products.All.Count }));
app.Run();
