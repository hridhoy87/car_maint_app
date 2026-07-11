package hobby.asad.mushad.mycar.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(
    tableName = "expenditures",
    foreignKeys = {
        @ForeignKey(
            entity = Vehicle.class,
            parentColumns = "id",
            childColumns = "vehicle_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Expenditure.class,
            parentColumns = "id",
            childColumns = "parent_id",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {
        @Index("vehicle_id"),
        @Index("parent_id")
    }
)
public class Expenditure {

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "vehicle_id")
    @NonNull
    public String vehicleId;

    @ColumnInfo(name = "parent_id")
    public String parentId;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "date")
    public long date;

    @ColumnInfo(name = "odometer")
    public int odometer;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "shop_name")
    public String shopName;

    @ColumnInfo(name = "fuel_quantity")
    public Double fuelQuantity;

    @ColumnInfo(name = "fuel_price_per_unit")
    public Double fuelPricePerUnit;

    @ColumnInfo(name = "summary")
    public String summary;

    public Expenditure() {
        this.id = UUID.randomUUID().toString();
    }
}
