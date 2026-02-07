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
////3d1c71d4819701c9dd664b270ae489fe - API key Минска

package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Main {

    private static final String API_BASE = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "3d1c71d4819701c9dd664b270ae489fe";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private JFrame frame;

    private JTextField cityField;

    private JRadioButton byCityRadio;
    private JRadioButton byCoordRadio;

    private JTextField latField;
    private JTextField lonField;

    private JButton fetchButton;
    private JButton clearButton;

    private JLabel statusLabel;
    private JTextArea outputArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }

    private void start() {
        frame = new JFrame("Погода (OpenWeatherMap)");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(720, 520));

        frame.setContentPane(buildUi());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(buildTopPanel(), BorderLayout.NORTH);
        root.add(buildOutputPanel(), BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildTopPanel() {
        JPanel top = new JPanel(new BorderLayout(12, 12));

        JPanel inputs = new JPanel(new GridBagLayout());
        inputs.setBorder(BorderFactory.createTitledBorder("Параметры запроса"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        byCityRadio = new JRadioButton("По городу", true);
        byCoordRadio = new JRadioButton("По координатам");

        ButtonGroup group = new ButtonGroup();
        group.add(byCityRadio);
        group.add(byCoordRadio);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        modePanel.add(byCityRadio);
        modePanel.add(byCoordRadio);

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        inputs.add(new JLabel("Режим:"), c);

        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        inputs.add(modePanel, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        inputs.add(new JLabel("Город:"), c);

        cityField = new JTextField();
        cityField.setToolTipText("Например: Minsk, Москва, New York");
        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        inputs.add(cityField, c);

        JPanel coordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        cc.fill = GridBagConstraints.HORIZONTAL;

        latField = new JTextField();
        lonField = new JTextField();

        latField.setToolTipText("Широта, например 53.902735");
        lonField.setToolTipText("Долгота, например 27.555696");

        cc.gridx = 0; cc.gridy = 0; cc.weightx = 1;
        cc.insets = new Insets(0, 0, 0, 6);
        coordPanel.add(latField, cc);

        cc.gridx = 1; cc.gridy = 0; cc.weightx = 1;
        cc.insets = new Insets(0, 6, 0, 0);
        coordPanel.add(lonField, cc);

        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        inputs.add(new JLabel("Координаты (lat / lon):"), c);

        c.gridx = 1; c.gridy = 2; c.weightx = 1;
        inputs.add(coordPanel, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        clearButton = new JButton("Очистить");
        fetchButton = new JButton("Получить погоду");

        buttons.add(clearButton);
        buttons.add(fetchButton);

        top.add(inputs, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        fetchButton.addActionListener(e -> onFetch());
        clearButton.addActionListener(e -> onClear());

        byCityRadio.addActionListener(e -> updateModeUi());
        byCoordRadio.addActionListener(e -> updateModeUi());

        updateModeUi();

        return top;
    }

    private JComponent buildOutputPanel() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Результат"));

        return scroll;
    }

    private JComponent buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(12, 12));
        statusLabel = new JLabel("Готово.");
        bottom.add(statusLabel, BorderLayout.CENTER);
        return bottom;
    }

    private void updateModeUi() {
        boolean byCity = byCityRadio.isSelected();

        cityField.setEnabled(byCity);
        latField.setEnabled(!byCity);
        lonField.setEnabled(!byCity);

        if (byCity) {
            latField.setText("");
            lonField.setText("");
        } else {
            cityField.setText("");
        }
    }

    private void onClear() {
        cityField.setText("");
        latField.setText("");
        lonField.setText("");
        outputArea.setText("");
        statusLabel.setText("Готово.");
        byCityRadio.setSelected(true);
        updateModeUi();
    }

    private void onFetch() {
        URI uri;
        try {
            uri = buildUri();
        } catch (Exception ex) {
            showError(ex.getMessage());
            return;
        }

        setBusy(true, "Отправляю запрос...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> SwingUtilities.invokeLater(() -> {
                    try {
                        handleResponse(response);
                    } finally {
                        setBusy(false, "Готово.");
                    }
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        setBusy(false, "Ошибка запроса.");
                        showError("Ошибка запроса: " + safeMessage(ex));
                    });
                    return null;
                });
    }

    private URI buildUri() {
        String units = "metric";
        String lang = "ru";

        String query;
        if (byCityRadio.isSelected()) {
            String city = cityField.getText().trim();
            if (city.isEmpty()) {
                throw new IllegalArgumentException("Поле 'Город' пустое.");
            }

            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            query = "q=" + encodedCity
                    + "&appid=" + URLEncoder.encode(API_KEY, StandardCharsets.UTF_8)
                    + "&units=" + units
                    + "&lang=" + lang;
        } else {
            String lat = latField.getText().trim();
            String lon = lonField.getText().trim();

            if (lat.isEmpty() || lon.isEmpty()) {
                throw new IllegalArgumentException("Для режима координат нужно заполнить и lat, и lon.");
            }

            parseDoubleStrict(lat, "lat");
            parseDoubleStrict(lon, "lon");


            query = "lat=" + URLEncoder.encode(normalizeNumber(lat), StandardCharsets.UTF_8)
                    + "&lon=" + URLEncoder.encode(normalizeNumber(lon), StandardCharsets.UTF_8)
                    + "&appid=" + URLEncoder.encode(API_KEY, StandardCharsets.UTF_8)
                    + "&units=" + units
                    + "&lang=" + lang;
        }

        return URI.create(API_BASE + "?" + query);
    }

    private void handleResponse(HttpResponse<String> response) {
        int code = response.statusCode();
        String body = response.body();

        if (code != 200) {
            outputArea.setText("");
            statusLabel.setText("Ошибка: HTTP " + code);
            showError("HTTP " + code + "\n\n" + body);
            return;
        }

        JSONObject json = new JSONObject(body);

        String cityName = json.optString("name", "—");
        String country = "—";
        if (json.has("sys")) {
            country = json.getJSONObject("sys").optString("country", "—");
        }

        JSONObject main = json.optJSONObject("main");
        double temp = main != null ? main.optDouble("temp", Double.NaN) : Double.NaN;
        double feels = main != null ? main.optDouble("feels_like", Double.NaN) : Double.NaN;
        int humidity = main != null ? main.optInt("humidity", -1) : -1;
        int pressure = main != null ? main.optInt("pressure", -1) : -1;

        String description = "—";
        JSONArray weatherArr = json.optJSONArray("weather");
        if (weatherArr != null && !weatherArr.isEmpty()) {
            JSONObject w0 = weatherArr.optJSONObject(0);
            if (w0 != null) {
                description = w0.optString("description", "—");
            }
        }

        double wind = Double.NaN;
        JSONObject windObj = json.optJSONObject("wind");
        if (windObj != null) {
            wind = windObj.optDouble("speed", Double.NaN);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Локация: ").append(cityName).append(" (").append(country).append(")\n");
        sb.append("Погода: ").append(description).append("\n\n");

        sb.append("Температура: ").append(formatNum(temp)).append(" °C\n");
        sb.append("Ощущается как: ").append(formatNum(feels)).append(" °C\n");
        sb.append("Влажность: ").append(humidity >= 0 ? humidity + " %" : "—").append("\n");
        sb.append("Давление: ").append(pressure >= 0 ? pressure + " hPa" : "—").append("\n");
        sb.append("Ветер: ").append(formatNum(wind)).append(" м/с\n");

        outputArea.setText(sb.toString());
        outputArea.setCaretPosition(0);
        statusLabel.setText("Успешно: HTTP 200");
    }

    private void setBusy(boolean busy, String status) {
        fetchButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);

        byCityRadio.setEnabled(!busy);
        byCoordRadio.setEnabled(!busy);

        boolean byCity = byCityRadio.isSelected();
        cityField.setEnabled(!busy && byCity);
        latField.setEnabled(!busy && !byCity);
        lonField.setEnabled(!busy && !byCity);

        statusLabel.setText(status);
        frame.setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private static void parseDoubleStrict(String value, String field) {
        try {
            Double.parseDouble(normalizeNumber(value));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Некорректное число в поле " + field + ": " + value);
        }
    }

    private static String normalizeNumber(String value) {
        return value.replace(',', '.');
    }

    private static String formatNum(double v) {
        if (Double.isNaN(v)) return "—";
        return String.format(java.util.Locale.US, "%.1f", v).replace('.', ',');
    }

    private static String safeMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null) cur = cur.getCause();
        String msg = cur.getMessage();
        return msg == null ? cur.getClass().getSimpleName() : msg;
    }
}
