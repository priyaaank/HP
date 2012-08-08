package com.thoughtworks.hp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.thoughtworks.hp.CustomWindow;
import com.thoughtworks.hp.R;
import com.thoughtworks.hp.adapters.BuyListAdapter;
import com.thoughtworks.hp.adapters.ProductListAdapter;
import com.thoughtworks.hp.models.Product;
import com.thoughtworks.hp.models.ShoppingList;
import com.thoughtworks.hp.models.ShoppingListProduct;
import com.thoughtworks.hp.presenters.ShoppingListPresenter;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends CustomWindow implements TextWatcher {

    private ShoppingListPresenter shoppingListScreen;

    private ProductListAdapter autoSuggestAdapter;
    private ListView autoSuggestListView;
    private List<Product> autoSuggestedProductList = new ArrayList<Product>();

    private BuyListAdapter shoppingListProductAdapter;
    private List<ShoppingListProduct> toBuyProductList = new ArrayList<ShoppingListProduct>();
    private long shoppingListId;

	private BarcodeScanner barcodeScanner;

    private TextView costOfShoppingList;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.shopping_list_detail_listing);

        this.shoppingListId = getIntent().getLongExtra(ShoppingList.SHOPPING_LIST_ID, 1);
        shoppingListScreen = new ShoppingListPresenter(ShoppingList.findById(shoppingListId));

        bindBarcodeScanner();
        initToBuyListView();
        initAutoSuggestListView();
        refreshShoppingListCost();
        attachSelfAsTextWatcherToSearchBox();
    }

    private void bindBarcodeScanner() {
        barcodeScanner = new BarcodeScanner(this, (ImageView) this.findViewById(R.id.scan_upc_button));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Product productFromBarcode = barcodeScanner.fetchProductFromBarcodeData(requestCode, resultCode, intent);
        if(productFromBarcode != null) {
            addAndPersistProductInShoppingList(productFromBarcode);
        }
    }

    private void initToBuyListView() {
        this.toBuyProductList.addAll(shoppingListScreen.shoppingListProducts());
        this.shoppingListProductAdapter = new BuyListAdapter(this, R.layout.product_line_item, toBuyProductList);
        ListView toBuyProductListView = (ListView) this.findViewById(R.id.shopping_list_product_listing);
        toBuyProductListView.setAdapter(this.shoppingListProductAdapter);
    }

    private void initAutoSuggestListView() {
        this.autoSuggestAdapter = new ProductListAdapter(this, R.layout.product_auto_suggest_line_item, autoSuggestedProductList);
        this.autoSuggestListView = (ListView) this.findViewById(R.id.auto_suggest_list);
        autoSuggestListView.setAdapter(this.autoSuggestAdapter);
        autoSuggestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Product product = autoSuggestedProductList.get(position);
                addAndPersistProductInShoppingList(product);
            }
        });
    }

    private void addAndPersistProductInShoppingList(Product product) {
        shoppingListScreen.addSelectedProductToShoppingList(product);
        resetCompleteView();
    }

    private void refreshShoppingListCost() {
        costOfShoppingList = (TextView) findViewById(R.id.list_detail_total_item_unit_value);
        costOfShoppingList.setText(shoppingListScreen.totalShoppingListCost());
    }

    private void resetCompleteView() {
        resetToBuyList();
        refreshShoppingListCost();
        resetAutoSuggestList();
    }

    private void resetAutoSuggestList() {
        autoSuggestedProductList.clear();
        autoSuggestAdapter.notifyDataSetChanged();
        ((EditText)AddProductActivity.this.findViewById(R.id.search_product_box)).setText("");
        autoSuggestListView.setVisibility(View.INVISIBLE);
    }

    private void resetToBuyList() {
        toBuyProductList.clear();
        toBuyProductList.addAll(shoppingListScreen.shoppingListProducts());
        shoppingListProductAdapter.notifyDataSetChanged();
    }

    private void attachSelfAsTextWatcherToSearchBox() {
        ((EditText)this.findViewById(R.id.search_product_box)).addTextChangedListener(this);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (charSequence == null || charSequence.length() < 1) return;

        autoSuggestListView.setVisibility(View.VISIBLE);
        autoSuggestedProductList.clear();
        autoSuggestedProductList.addAll(Product.findProductsByMatchingName(charSequence.toString()));
        clearAlreadyAddedProductsFrom(autoSuggestedProductList);
        autoSuggestAdapter.notifyDataSetChanged();
    }

    private void clearAlreadyAddedProductsFrom(List<Product> autoSuggestedProductList) {
//        autoSuggestedProductList.removeAll(this.toBuyProductList);
    }

    @Override
    public void afterTextChanged(Editable editable) { }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }
}