package org.example.tema2.structure.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.example.tema2.structure.Drink;
import org.example.tema2.structure.Food;
import org.example.tema2.structure.Product;

import java.io.IOException;

public class ProductDeserializer extends StdDeserializer<Product> {

    public ProductDeserializer() {
        super(Product.class);
    }

    @Override
    public Product deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        if (!node.has("name")) {
            throw new JsonProcessingException("Product is missing the required 'name' field.") {};
        }
        String name = node.get("name").asText();

        if (!node.has("price")) {
            throw new JsonProcessingException("Product '" + name + "' is missing the required 'price' field.") {};
        }
        double price = node.get("price").asDouble();

        if (!node.has("type")) {
            throw new JsonProcessingException("Product '" + name + "' is missing the required 'type' field.") {};
        }
        Product.Type type = Product.Type.valueOf(node.get("type").asText());

        boolean isVegetarian = node.has("vegetarian") && node.get("vegetarian").asBoolean();

        if (node.has("weight")) {
            int weight = node.get("weight").asInt();
            return new Food(name, price, weight, type, isVegetarian);
        } else if (node.has("volume")) {
            int volume = node.get("volume").asInt();
            return new Drink(name, price, volume, type, isVegetarian);
        } else {
            throw new JsonProcessingException("Product '" + name + "' must have either a 'weight' or 'volume' field.") {};
        }
    }
}
