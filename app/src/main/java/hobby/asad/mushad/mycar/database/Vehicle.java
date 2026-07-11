package hobby.asad.mushad.mycar.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "vehicles")
public class Vehicle {

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "model")
    public String model;

    @ColumnInfo(name = "registration_number")
    public String registrationNumber;

    @ColumnInfo(name = "color")
    public String color;

    @ColumnInfo(name = "current_odometer")
    public int currentOdometer;

    @ColumnInfo(name = "transmission_type")
    public String transmissionType;

    @ColumnInfo(name = "chassis_number")
    public String chassisNumber;

    @ColumnInfo(name = "tank_capacity")
    public double tankCapacity;

    @ColumnInfo(name = "engine_oil_capacity")
    public double engineOilCapacity;

    public Vehicle() {
        this.id = UUID.randomUUID().toString();
    }
}
