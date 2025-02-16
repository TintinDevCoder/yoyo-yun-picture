package com.dd.yunpicturebackend;
import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchAPI;
import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchManager;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImageObject;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImagesModel;
public class Yitusoutu {
    public static void main(String[] args) {
        final String subscriptionKey = "COPY_YOUR_KEY_HERE";
        String searchTerm = "canadian rockies";
        //Image search client
        BingImageSearchAPI client = BingImageSearchManager.authenticate(subscriptionKey);
        ImagesModel imageResults = client.bingImages().search()
                .withQuery(searchTerm)
                .withMarket("en-us")
                .execute();
    }
}
