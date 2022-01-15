# SQLi Izvestaj

U ovom zadatku je iskoriscen SQLi napad pomocu instrukcije:

```sql
'); INSERT INTO restaurant (id, name, address, typeid) VALUES (666, 'Aleja Pilica', 'Kokos I Mokos 22', 2); INSERT INTO food(id, name, price, restaurantId) VALUES (666, 'Veganska Svinja', 999, 666); --
```

![Rezultat](SQL%20result.png)

Kao sto se vidi na slici, "Aleja Pilica" je dodat kao opcija za biranje.

## Zastita

U resenju je uocena slabost da se konkateniraju Stringovi bez ikakve provere. 

```java

public void insertNewOrder(NewOrder newOrder, int userId) {
    LocalDate date = LocalDate.now();
    String sqlQuery = "INSERT INTO delivery (isDone, userId, restaurantId, addressId, date, comment)" +
            "values (FALSE, " + userId + ", " + newOrder.getRestaurantId() + ", " + newOrder.getAddress() + "," +
            "'" + date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth() + "', '" + newOrder.getComment() + "')";
    try {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(sqlQuery);

```
Kako bismo resili ovo, a ujedno sto manje koda izmenili, neophodno je koristiti ``` preparedStatement ``` umesto ``` createStatement ```

Kada izmenimo, dobijamo sledeci kod:

```java

public void insertNewOrder(NewOrder newOrder, int userId) {
        LocalDate date = LocalDate.now();
        String sqlQuery = "INSERT INTO delivery (isDone, userId, restaurantId, addressId, date, comment) VALUES (FALSE, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, userId);
            statement.setInt(2, newOrder.getRestaurantId());
            statement.setInt(3, newOrder.getAddress());
            statement.setString(4, date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth());
            statement.setString(5, newOrder.getComment());
            statement.executeUpdate();

```

Ako bismo istestirali ovaj kod, trebalo bi da sve ostane nepromenjeno:

### PRE

<img src="pre.png" alt="PRE" style="zoom: 67%;" />

### POSLE

![POSLE1](posle1.png)

Ali:

![Posle](posle2.png)

Vidimo da je naredba "obradjena" i upisana u tabelu!
