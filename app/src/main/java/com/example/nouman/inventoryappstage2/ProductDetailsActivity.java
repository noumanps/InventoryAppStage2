package com.example.nouman.inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nouman.inventoryappstage2.Data.ProductContract.ProductEntry;

public class ProductDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentProductUri;
    private ImageView mCurrentImage;
    private TextView mCurrentName;
    private TextView mCurrentPrice;
    private TextView mCurrentQuantity;
    private TextView mCurrentSupplier;
    private TextView mCurrentSupplierContact;
    private Button callButton, increaseButton, decreaseButton, editButton, deleteButton;
    private String supplierContact;
    private long id;
    private int quantity;

    private static final int EXISTING_PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        mCurrentImage = findViewById(R.id.product_image);
        mCurrentName = findViewById(R.id.product_name);
        mCurrentPrice = findViewById(R.id.product_price);
        mCurrentQuantity = findViewById(R.id.product_quantity);
        mCurrentSupplier = findViewById(R.id.product_supplier);
        mCurrentSupplierContact = findViewById(R.id.product_supplier_contact);
        callButton = findViewById(R.id.call_button);
        increaseButton = findViewById(R.id.increase_button);
        decreaseButton = findViewById(R.id.decrease_button);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditor(id);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = "tel:" + supplierContact;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(number));
                //Verify that the intent will resolve to at least one activity
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saleProduct(id, quantity);

            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct(id, quantity);

            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_CONTACT,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierContactColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_CONTACT);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            id = cursor.getInt(idColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            supplierContact = cursor.getString(supplierContactColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mCurrentName.setText("Product Name: " + name);
            mCurrentPrice.setText("Product Price: " + Integer.toString(price));
            mCurrentQuantity.setText("Available Quantity: " + Integer.toString(quantity));
            mCurrentSupplier.setText("Product Supplier: " + supplier);
            mCurrentSupplierContact.setText("Supplier Contact: " + supplierContact);
            mCurrentImage.setImageURI(Uri.parse(image));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mCurrentName.setText("");
        mCurrentPrice.setText("");
        mCurrentQuantity.setText("");
        mCurrentSupplier.setText("");
        mCurrentSupplierContact.setText("");
        mCurrentImage = null;
    }

    public void openEditor(long id) {
        // Create new intent to go to {@link EditorActivity}
        Intent intent = new Intent(ProductDetailsActivity.this, EditorActivity.class);

        // Form the content URI that represents the specific product that was clicked on,
        // by appending the "id" (passed as input to this method) onto the
        // {@link ProductEntry#CONTENT_URI}.
        // For example, the URI would be "content://com.example.android.inventoryapp/products/2"
        // if the product with ID 2 was clicked on.
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        // Set the URI on the data field of the intent
        intent.setData(currentProductUri);

        // Launch the {@link EditorActivity} to display the data for the current product.
        startActivity(intent);
    }

    public void saleProduct(long id, int quantity) {
        if (quantity >= 1) {
            quantity--;

            Uri updateUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            int rowUpdated = getContentResolver().update(updateUri, values, null, null);
            if (rowUpdated == 1) {
                Toast.makeText(this, R.string.product_sold, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_sale_fail, Toast.LENGTH_SHORT).show();
            }
        } else Toast.makeText(this, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
    }

    public void addProduct(long id, int quantity) {
        quantity++;

        Uri updateUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        int rowUpdated = getContentResolver().update(updateUri, values, null, null);
        if (rowUpdated == 1) {
            Toast.makeText(this, R.string.product_added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.product_addition_fail, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
