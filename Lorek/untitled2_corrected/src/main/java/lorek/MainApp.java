package lorek;

import java.sql.*;
import java.util.Scanner;

public class MainApp {
    private Connection connection;

    public MainApp(Connection connection) {
        this.connection = connection;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Меню:");
            System.out.println("1 - Добавить товар");
            System.out.println("2 - Редактировать товар");
            System.out.println("3 - Продать товар");
            System.out.println("4 - Показать все товары");
            System.out.println("5 - Выход");
            System.out.print("Выберите опцию: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    this.addProduct(scanner);
                    break;
                case 2:
                    this.editProduct(scanner);
                    break;
                case 3:
                    this.sellProduct(scanner);
                    break;
                case 4:
                    this.showAllProducts();
                    break;
                case 5:
                    System.out.println("Выход из программы.");
                    return;
                default:
                    System.out.println("Неверный выбор, попробуйте снова.");
            }
        }
    }

    private void addProduct(Scanner scanner) {
        System.out.print("Введите название товара: ");
        String name = scanner.nextLine();
        System.out.print("Введите новую категорию (Газета, Журнал, Книга): ");
        String category = scanner.nextLine();
        System.out.print("Введите количество товара: ");
        int quantity = scanner.nextInt();
        System.out.print("Введите цену товара: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        executeInsertProduct(name, category, quantity, price);
    }

    private void editProduct(Scanner scanner) {
        System.out.print("Введите ID товара для редактирования: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Введите новое название товара: ");
        String name = scanner.nextLine();
        System.out.print("Введите новое количество товара: ");
        int quantity = scanner.nextInt();
        System.out.print("Введите новую цену товара: ");
        double price = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Введите новую категорию (Газета, Журнал, Книга): ");
        String category = scanner.nextLine();

        executeUpdateProduct(id, name, quantity, price, category);
    }

    private void sellProduct(Scanner scanner) {
        System.out.print("Введите ID товара для продажи: ");
        int id = scanner.nextInt();
        System.out.print("Введите количество для продажи: ");
        int quantity = scanner.nextInt();

        executeSellProduct(id, quantity);
    }

    private void showAllProducts() {
        executeShowAllProducts();
    }

    private void executeInsertProduct(String name, String category, int quantity, double price) {
        String query = "INSERT INTO products (name, quantity, price, category) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setInt(2, quantity);
            statement.setDouble(3, price);
            statement.setString(4, category);
            statement.executeUpdate();
            System.out.println("Товар успешно добавлен.");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении товара: " + e.getMessage());
        }
    }

    private void executeUpdateProduct(int id, String name, int quantity, double price, String category) {
        String query = "UPDATE products SET name = ?, quantity = ?, price = ?, category = ? WHERE id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setInt(2, quantity);
            statement.setDouble(3, price);
            statement.setString(4, category);
            statement.setInt(5, id);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Товар успешно отредактирован.");
            } else {
                System.out.println("Товар с таким ID не найден.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при редактировании товара: " + e.getMessage());
        }
    }

    private void executeSellProduct(int id, int quantity) {
        String selectQuery = "SELECT quantity FROM products WHERE id = ?";
        String updateQuery = "UPDATE products SET quantity = ? WHERE id = ?";

        try (
                PreparedStatement selectStmt = this.connection.prepareStatement(selectQuery);
                PreparedStatement updateStmt = this.connection.prepareStatement(updateQuery);
        ) {
            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                if (quantity > currentQuantity) {
                    System.err.println("Ошибка: Недостаточно товара. Доступно: " + currentQuantity);
                } else {
                    int newQuantity = currentQuantity - quantity;
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    System.out.println("Товар успешно продан. Оставшееся количество: " + newQuantity);
                    if (newQuantity == 0) {
                        // Удаление товара исключено
                    }
                }
            } else {
                System.err.println("Ошибка: Товар с ID " + id + " не найден.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при продаже товара: " + e.getMessage());
        }
    }


    private void executeShowAllProducts() {
        String query = "SELECT * FROM products";
        try (
                Statement statement = this.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
        ) {
            System.out.println("Товары в базе данных:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                String category = resultSet.getString("category");
                System.out.println("ID: " + id + ", Название: " + name + ", Категория: " + category + ", Количество: " + quantity + ", Цена: " + price);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при выводе товаров: " + e.getMessage());
        }
    }
}