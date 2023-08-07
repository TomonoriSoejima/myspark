import java.text.SimpleDateFormat;
import java.util.*;

public class Hotel{
    int star;
    int price;
    int capacity;
    String hotel_name;
    String group;
    String country;
    String city;
    String busy_month;
    String created;
    String has_parking;
    List<String> menu;
    List<String> closed;



    public Hotel (int star, int price, int capacity,
                 String hotel_name, String group, String country,
                 String city, String busy_month,
                 String created, String has_parking,
                 List<String> menu, List<String> closed
                 )
    {
        this.star = star;
        this.price = price;
        this.capacity = capacity;
        this.hotel_name = hotel_name;
        this.group = group;
        this.country = country;
        this.city = city;
        this.busy_month = busy_month;
        this.created = created;
        this.has_parking = has_parking;
        this.menu = menu;
        this.closed = closed;

    }

    public static List<Hotel> make_hotels(int how_many, Helper.INTERVAL interval) {
        Random random_number = new Random();
        long seconds = Helper.getCurrentTime();

        List<Hotel> hotels = new ArrayList<>();

        for (int i = 1; i <= how_many; i++ ) {
            int star = random_number.nextInt(30);
            int price = 1000 + random_number.nextInt(40000);
            int capacity = 10 + random_number.nextInt(2000);
            String hotel_name = "hotel" + i;

            String group = RandomData.Group.randomGroup().toString();
            String country = RandomData.Country.randomCountry().toString();
            String city = RandomData.JapanCity.randomJapanCity().toString();
            String busy_month = RandomData.Month.randomMonth().toString();

            seconds = Helper.setTimeOffset(seconds, interval);
            Date created = new Date(seconds*1000L);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String created_string = dateFormat.format(created);


            String has_parking = i % 2 == 0 ? "yes" : "no";
            List<String> menus = Hotel.setMenu();
            List<String> closed = Hotel.setCloesd();

            Hotel hotel = new Hotel(star, price, capacity, hotel_name, group, country, city,
                    busy_month, created_string, has_parking,menus, closed);

            hotels.add(hotel);

        }

        return hotels;

    }

    public static List<String> setMenu() {
        String menu1 = RandomData.FoodMenu.randomFoodMenu().toString();
        String menu2 = RandomData.FoodMenu.randomFoodMenu().toString();
        String menu3 = RandomData.FoodMenu.randomFoodMenu().toString();

        String[] menu = {menu1,menu2,menu3};
        return Arrays.asList(menu);
    }

    public static List<String> setCloesd() {
        String closed1 = RandomData.Day.randomDay().toString();
        String closed2 = RandomData.Day.randomDay().toString();
        String[] closed = {closed1,closed2};

        return Arrays.asList(closed);
    }





        // getters
        public int getStar() {
            return star;
        }

        public int getPrice() {
            return price;
        }

        public int getCapacity() {
            return capacity;
        }

        public String getHotel_name() {
            return hotel_name;
        }

        public String getGroup() {
            return group;
        }

        public String getCountry() {
            return country;
        }

        public String getCity() {
            return city;
        }

        public String getBusy_month() {
            return busy_month;
        }

        public String getCreated() {
            return created;
        }

        public String getHas_parking() {
            return has_parking;
        }

        public List<String> getMenu() {
            return menu;
        }

        public List<String> getClosed() {
            return closed;
        }

        // setters
        public void setStar(int star) {
            this.star = star;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public void setHotel_name(String hotel_name) {
            this.hotel_name = hotel_name;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setBusy_month(String busy_month) {
            this.busy_month = busy_month;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public void setHas_parking(String has_parking) {
            this.has_parking = has_parking;
        }

        public void setMenu(List<String> menu) {
            this.menu = menu;
        }

        public void setClosed(List<String> closed) {
            this.closed = closed;
        }


}