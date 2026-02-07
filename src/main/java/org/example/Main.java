//package org.example;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Scanner;
//
//public class Main {
//
//    public static void main(String[] args) throws Exception {
//        Scanner sc = new Scanner(System.in);
//
//        System.out.print("Город: ");
//        String city = sc.nextLine();
//
//        System.out.print("API key: ");
//        String apiKey = sc.nextLine();
//
//        String url =
//                "https://api.openweathermap.org/data/2.5/weather"
//                        + "?q=" + city
//                        + "&appid=" + apiKey
//                        + "&units=metric"
//                        + "&lang=ru";
//
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .GET()
//                .build();
//
//        HttpResponse<String> response =
//                client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() != 200) {
//            System.out.println("Ошибка: " + response.statusCode());
//            System.out.println(response.body());
//            return;
//        }
//
//        JSONObject json = new JSONObject(response.body());
//
//        JSONObject main = json.getJSONObject("main");
//        double temp = main.getDouble("temp");
//        double feelsLike = main.getDouble("feels_like");
//        int humidity = main.getInt("humidity");
//        int pressureHpA = main.getInt("pressure");// Давление в гПа
//        long pressure = Math.round(pressureHpA * 0.75006);//Давление в мм рт. ст.
//        double windSpeed = json.getJSONObject("wind").getDouble("speed");
//
//        JSONArray weatherArr = json.getJSONArray("weather");
//        String description = weatherArr.getJSONObject(0).getString("description");
//
//        System.out.println("Погода в городе " + city + " ");
//        System.out.println("Описание: " + description);
//        System.out.println("Температура: " + temp + "°C (ощущается как " + feelsLike + "°C)");
//        System.out.println("Влажность: " + humidity + "%");
//        System.out.println("Давление: " + pressure + " мм. рт. ст.");
//        System.out.println("Скорость ветра: " + windSpeed + " м/с");
//    }
//}
////3d1c71d4819701c9dd664b270ae489fe - API key



package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Main {

    private static final String API_KEY = "3d1c71d4819701c9dd664b270ae489fe";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private JFrame frame;
    private JPanel rootPanel;

    private JTextField cityField, latField, lonField;
    private JRadioButton byCityRadio, byCoordRadio;
    private JButton weatherButton, forecastButton, clearButton;

    private JTextArea outputArea;
    private JLabel statusLabel, iconLabel;

    private boolean isRussian = true;
    private String currentLang = "ru";

    private JLabel modeLabel, cityLabel, coordLabel;
    private TitledBorder paramsBorder, resultBorder;

    private JMenu menuLanguage, menuColor, menuAbout;
    private JMenuItem menuRu, menuEn;

    private final Map<String, Color> colors = new LinkedHashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }

    private void start() {
        initColors();

        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 520));

        frame.setJMenuBar(buildMenu());
        frame.setContentPane(buildUi());
        frame.setLocationRelativeTo(null);

        applyLanguage();
        frame.setVisible(true);
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();

        menuLanguage = new JMenu();
        menuRu = new JMenuItem();
        menuEn = new JMenuItem();

        menuRu.addActionListener(e -> switchLanguage(true));
        menuEn.addActionListener(e -> switchLanguage(false));

        menuLanguage.add(menuRu);
        menuLanguage.add(menuEn);

        menuColor = new JMenu();
        buildColorMenu();

        menuAbout = new JMenu();
        JMenuItem aboutItem = new JMenuItem();
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(frame,
                        tr("Программа погоды\nИспользует OpenWeatherMap API",
                                "Weather application\nUses OpenWeatherMap API"),
                        tr("О программе", "About"),
                        JOptionPane.INFORMATION_MESSAGE
                )
        );
        menuAbout.add(aboutItem);

        bar.add(menuLanguage);
        bar.add(menuColor);
        bar.add(menuAbout);

        return bar;
    }

    private void initColors() {
        colors.put("WHITE", Color.WHITE);
        colors.put("LIGHT_GRAY", Color.LIGHT_GRAY);
        colors.put("GRAY", Color.GRAY);
        colors.put("PINK", Color.PINK);
        colors.put("CYAN", Color.CYAN);
        colors.put("YELLOW", Color.YELLOW);
        colors.put("ORANGE", Color.ORANGE);
        colors.put("GREEN", Color.GREEN);
        colors.put("BLUE", new Color(170, 200, 255));
        colors.put("VIOLET", new Color(200, 170, 255));
    }

    private void buildColorMenu() {
        menuColor.removeAll();
        for (String key : colors.keySet()) {
            JMenuItem item = new JMenuItem(tr(colorRu(key), colorEn(key)));
            item.addActionListener(e -> setBackgroundColor(colors.get(key)));
            menuColor.add(item);
        }
    }

    private String colorRu(String key) {
        return switch (key) {
            case "WHITE" -> "Белый";
            case "LIGHT_GRAY" -> "Светло-серый";
            case "GRAY" -> "Серый";
            case "PINK" -> "Розовый";
            case "CYAN" -> "Голубой";
            case "YELLOW" -> "Жёлтый";
            case "ORANGE" -> "Оранжевый";
            case "GREEN" -> "Зелёный";
            case "BLUE" -> "Синий";
            case "VIOLET" -> "Фиолетовый";
            default -> key;
        };
    }

    private String colorEn(String key) {
        return switch (key) {
            case "WHITE" -> "White";
            case "LIGHT_GRAY" -> "Light gray";
            case "GRAY" -> "Gray";
            case "PINK" -> "Pink";
            case "CYAN" -> "Cyan";
            case "YELLOW" -> "Yellow";
            case "ORANGE" -> "Orange";
            case "GREEN" -> "Green";
            case "BLUE" -> "Blue";
            case "VIOLET" -> "Violet";
            default -> key;
        };
    }

    private void setBackgroundColor(Color color) {
        setBgRecursive(rootPanel, color);
        rootPanel.repaint();
    }

    private void setBgRecursive(Component c, Color color) {
        c.setBackground(color);
        if (c instanceof Container cont) {
            for (Component child : cont.getComponents()) {
                setBgRecursive(child, color);
            }
        }
    }

    private JPanel buildUi() {
        rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        rootPanel.add(buildTopPanel(), BorderLayout.NORTH);
        rootPanel.add(buildOutputPanel(), BorderLayout.CENTER);
        rootPanel.add(buildBottomPanel(), BorderLayout.SOUTH);

        return rootPanel;
    }

    private JComponent buildTopPanel() {
        JPanel inputs = new JPanel(new GridBagLayout());
        paramsBorder = BorderFactory.createTitledBorder("");
        inputs.setBorder(paramsBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        byCityRadio = new JRadioButton();
        byCoordRadio = new JRadioButton();
        ButtonGroup group = new ButtonGroup();
        group.add(byCityRadio);
        group.add(byCoordRadio);
        byCityRadio.setSelected(true);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(byCityRadio);
        modePanel.add(byCoordRadio);

        modeLabel = new JLabel();
        c.gridx = 0; c.gridy = 0;
        inputs.add(modeLabel, c);
        c.gridx = 1;
        inputs.add(modePanel, c);

        cityLabel = new JLabel();
        c.gridx = 0; c.gridy = 1;
        inputs.add(cityLabel, c);

        cityField = new JTextField();
        c.gridx = 1;
        inputs.add(cityField, c);

        coordLabel = new JLabel();
        c.gridx = 0; c.gridy = 2;
        inputs.add(coordLabel, c);

        latField = new JTextField();
        lonField = new JTextField();

        JPanel coordPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        coordPanel.add(latField);
        coordPanel.add(lonField);

        c.gridx = 1;
        inputs.add(coordPanel, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearButton = new JButton();
        weatherButton = new JButton();
        forecastButton = new JButton();

        buttons.add(clearButton);
        buttons.add(weatherButton);
        buttons.add(forecastButton);

        clearButton.addActionListener(e -> onClear());
        weatherButton.addActionListener(e -> onWeather());
        forecastButton.addActionListener(e -> onForecast());

        byCityRadio.addActionListener(e -> updateMode());
        byCoordRadio.addActionListener(e -> updateMode());

        JPanel top = new JPanel(new BorderLayout());
        top.add(inputs, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        updateMode();
        return top;
    }

    private JComponent buildOutputPanel() {
        iconLabel = new JLabel("", JLabel.CENTER);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);

        JScrollPane scroll = new JScrollPane(outputArea);
        resultBorder = BorderFactory.createTitledBorder("");
        scroll.setBorder(resultBorder);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JComponent buildBottomPanel() {
        statusLabel = new JLabel();
        return statusLabel;
    }

    private void updateMode() {
        boolean byCity = byCityRadio.isSelected();
        cityField.setEnabled(byCity);
        latField.setEnabled(!byCity);
        lonField.setEnabled(!byCity);
    }

    private void onClear() {
        cityField.setText("");
        latField.setText("");
        lonField.setText("");
        outputArea.setText("");
        iconLabel.setIcon(null);
        statusLabel.setText(tr("Готово.", "Ready."));
    }

    private void onWeather() {
        try {
            URI uri = buildWeatherUri();
            sendRequest(uri, this::handleWeather);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onForecast() {
        try {
            URI uri = buildForecastUri();
            sendRequest(uri, this::handleForecast);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private URI buildWeatherUri() {
        if (byCityRadio.isSelected()) {
            String city = cityField.getText().trim();
            if (city.isEmpty()) throw new IllegalArgumentException(tr("Введите город", "Enter city"));
            return URI.create(WEATHER_URL + "?q=" + URLEncoder.encode(city, StandardCharsets.UTF_8) +
                    "&appid=" + API_KEY + "&units=metric&lang=" + currentLang);
        } else {
            String lat = latField.getText().trim();
            String lon = lonField.getText().trim();
            if (lat.isEmpty() || lon.isEmpty()) throw new IllegalArgumentException(tr("Введите координаты", "Enter coordinates"));
            return URI.create(WEATHER_URL + "?lat=" + lat + "&lon=" + lon +
                    "&appid=" + API_KEY + "&units=metric&lang=" + currentLang);
        }
    }

    private URI buildForecastUri() {
        if (byCityRadio.isSelected()) {
            String city = cityField.getText().trim();
            return URI.create(FORECAST_URL + "?q=" + URLEncoder.encode(city, StandardCharsets.UTF_8) +
                    "&appid=" + API_KEY + "&units=metric&lang=" + currentLang);
        } else {
            String lat = latField.getText().trim();
            String lon = lonField.getText().trim();
            return URI.create(FORECAST_URL + "?lat=" + lat + "&lon=" + lon +
                    "&appid=" + API_KEY + "&units=metric&lang=" + currentLang);
        }
    }

    private void sendRequest(URI uri, Consumer<HttpResponse<String>> handler) {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(r -> SwingUtilities.invokeLater(() -> handler.accept(r)));
    }

    private void handleWeather(HttpResponse<String> r) {
        JSONObject json = new JSONObject(r.body());
        JSONObject main = json.getJSONObject("main");
        JSONObject wind = json.getJSONObject("wind");
        JSONObject w0 = json.getJSONArray("weather").getJSONObject(0);

        setIcon(w0.getString("icon"));

        outputArea.setText(
                tr("Город: ", "City: ") + json.getString("name") + "\n" +

        tr("Погода: ", "Weather: ") + w0.getString("description") + "\n" +
                tr("Температура: ", "Temperature: ") + main.getDouble("temp") + " °C\n" +
                tr("Ветер: ", "Wind: ") + wind.getDouble("speed") + " m/s"
        );
    }

    private void handleForecast(HttpResponse<String> r) {
        JSONObject json = new JSONObject(r.body());
        JSONArray list = json.getJSONArray("list");

        StringBuilder sb = new StringBuilder(tr("Прогноз:\n\n", "Forecast:\n\n"));
        for (int i = 0; i < list.length(); i += 8) {
            JSONObject item = list.getJSONObject(i);
            JSONObject w = item.getJSONArray("weather").getJSONObject(0);
            JSONObject m = item.getJSONObject("main");

            sb.append(item.getString("dt_txt")).append("\n");
            sb.append(w.getString("description")).append(", ");
            sb.append(tr("темп.: ", "temp: ")).append(m.getDouble("temp")).append(" °C\n\n");
        }
        outputArea.setText(sb.toString());
        iconLabel.setIcon(null);
    }

    private void setIcon(String iconCode) {
        try {
            URL url = new URL("https://openweathermap.org/img/wn/" + iconCode + "@2x.png");
            iconLabel.setIcon(new ImageIcon(url));
        } catch (Exception e) {
            iconLabel.setIcon(null);
        }
    }

    private void switchLanguage(boolean ru) {
        isRussian = ru;
        currentLang = ru ? "ru" : "en";
        applyLanguage();
        buildColorMenu();
    }

    private void applyLanguage() {
        frame.setTitle(tr("Погода", "Weather"));

        menuLanguage.setText(tr("Выбор языка", "Language"));
        menuColor.setText(tr("Цвет фона", "Background color"));
        menuAbout.setText(tr("О программе", "About"));

        menuRu.setText(tr("Русский", "Russian"));
        menuEn.setText(tr("Английский", "English"));

        modeLabel.setText(tr("Режим:", "Mode:"));
        cityLabel.setText(tr("Город:", "City:"));
        coordLabel.setText(tr("Координаты (lat / lon):", "Coordinates (lat / lon):"));

        byCityRadio.setText(tr("По городу", "By city"));
        byCoordRadio.setText(tr("По координатам", "By coordinates"));

        weatherButton.setText(tr("Текущая погода", "Current weather"));
        forecastButton.setText(tr("Прогноз на 5 дней", "5-day forecast"));
        clearButton.setText(tr("Очистить", "Clear"));

        paramsBorder.setTitle(tr("Параметры", "Parameters"));
        resultBorder.setTitle(tr("Результат", "Result"));

    }

    private String tr(String ru, String en) {
        return isRussian ? ru : en;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg,
                tr("Ошибка", "Error"),
                JOptionPane.ERROR_MESSAGE);
    }
}