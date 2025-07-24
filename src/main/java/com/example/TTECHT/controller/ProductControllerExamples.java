package com.example.TTECHT.controller;

/**
 * API Usage Examples for Updated Product Controller
 * 
 * These examples show how to use the new color and size array functionality
 */

/*

Example 1: Create a T-shirt with multiple colors and sizes
POST /api/products
Content-Type: application/json

{
  "storeName": "Fashion Store",
  "categoryId": 1,
  "name": "Cotton T-Shirt",
  "description": "Comfortable cotton t-shirt for everyday wear",
  "price": 29.99,
  "stockQuantity": 100,
  "colors": ["Red", "Blue", "Green", "Black"],
  "sizes": ["S", "M", "L", "XL"],
  "brand": "CottonCo"
}

---

Example 2: Create a jacket with limited options
POST /api/products

{
  "storeName": "Winter Wear",
  "categoryId": 2,
  "name": "Winter Jacket",
  "description": "Warm winter jacket for cold weather",
  "price": 199.99,
  "stockQuantity": 25,
  "colors": ["Black", "Navy"],
  "sizes": ["L", "XL"],
  "brand": "WarmCorp"
}

---

Example 3: Create a product without sizes (one-size-fits-all)
POST /api/products

{
  "storeName": "Accessories Shop",
  "categoryId": 3,
  "name": "Baseball Cap",
  "description": "Adjustable baseball cap",
  "price": 24.99,
  "stockQuantity": 50,
  "colors": ["Red", "Blue", "White"],
  "sizes": null,
  "brand": "SportsCap"
}

---

Example 4: Create a product without colors
POST /api/products

{
  "storeName": "Electronics Store",
  "categoryId": 4,
  "name": "Wireless Headphones",
  "description": "Bluetooth wireless headphones",
  "price": 149.99,
  "stockQuantity": 30,
  "colors": null,
  "sizes": null,
  "brand": "AudioTech"
}

---

Response Example:
GET /api/products/1

{
  "productId": 1,
  "storeName": "Fashion Store",
  "categoryId": 1,
  "categoryName": "Clothing",
  "name": "Cotton T-Shirt",
  "description": "Comfortable cotton t-shirt for everyday wear",
  "price": 29.99,
  "stockQuantity": 100,
  "sellerUsername": "seller123",
  "sellerName": "John Doe",
  "soldQuantity": 15,
  "colors": ["Red", "Blue", "Green", "Black"],
  "sizes": ["S", "M", "L", "XL"],
  "brand": "CottonCo",
  "isBestSeller": false,
  "isNewArrival": true,
  "createdAt": "2024-01-15T10:30:00"
}

*/
