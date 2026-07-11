package hobby.asad.mushad.mycar.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenditureDao {
    @Insert
    void insert(Expenditure expenditure);

    @Update
    void update(Expenditure expenditure);

    @Delete
    void delete(Expenditure expenditure);

    @Query("SELECT * FROM expenditures WHERE vehicle_id = :vehicleId ORDER BY date DESC")
    List<Expenditure> getExpendituresForVehicle(String vehicleId);

    @Query("SELECT * FROM expenditures WHERE parent_id = :parentId ORDER BY date ASC")
    List<Expenditure> getChildExpenditures(String parentId);

    @Query("SELECT * FROM expenditures WHERE vehicle_id = :vehicleId AND parent_id IS NULL ORDER BY date DESC")
    List<Expenditure> getTopLevelExpenditures(String vehicleId);

    @Query("SELECT * FROM expenditures WHERE vehicle_id = :vehicleId AND category = :category ORDER BY date DESC")
    List<Expenditure> getExpendituresByCategory(String vehicleId, String category);

    @Query("SELECT SUM(amount) FROM expenditures WHERE vehicle_id = :vehicleId AND category = :category")
    Double getTotalAmountByCategory(String vehicleId, String category);

    @Query("SELECT SUM(amount) FROM expenditures WHERE vehicle_id = :vehicleId AND category = :category AND date >= :start AND date <= :end")
    Double getTotalAmountByCategoryAndDateRange(String vehicleId, String category, long start, long end);

    @Query("SELECT COUNT(*) FROM expenditures WHERE vehicle_id = :vehicleId")
    int getExpenditureCount(String vehicleId);

    @Query("SELECT * FROM expenditures WHERE title LIKE '%' || :keyword || '%' OR category = :keyword ORDER BY date DESC LIMIT 1")
    Expenditure getLastEntryByKeyword(String keyword);
}
