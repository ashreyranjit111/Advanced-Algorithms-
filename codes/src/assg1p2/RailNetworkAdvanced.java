package assg1p2;
 
import java.io.*;
import java.util.*;
 
import assg1p1.Station;
 
public class RailNetworkAdvanced {
 
    private TreeMap<String,Station> stationList;
    private TreeMap<String, TrainLine> lineList;
 
    private TreeMap<Station, ArrayList<String>> pathFromOrigin;
    private TreeMap<Station,TreeMap<Station, Integer>> distanceFromOrigin;
    private TreeMap<String,TreeMap<String,Integer>> getMinDistFromOrigin = new TreeMap<>();
   
 
    public RailNetworkAdvanced(String trainData, String connectionData, String linesData)
    {
        stationList = new TreeMap<>();
        lineList = new TreeMap<>();
 
        pathFromOrigin = new TreeMap<>();
        distanceFromOrigin = new TreeMap<>();
 
        try {
            readLinesData(linesData);
            readStationData(trainData);
            readConnectionData(connectionData);
        }
        catch (IOException e) {
            System.out.println("Exception encountered: " + e);
        }
    }
   
    /**
     * Reads the CSV file containing information about the lines
     *
     * @param infile
     * @throws IOException
     */
    public void readLinesData(String infile) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(infile));
 
        String s;
        s = in.readLine(); //skip the first line, csv headers
 
        while ((s = in.readLine()) != null)
        {
            String[] data = s.split(",");
            lineList.put(data[0], new TrainLine(data[0],
                                                data[1],
                                                data[2],
                                                data[3],
                                                Integer.valueOf(data[4])));
        }
        in.close();
    }
    /**
     * Reads the CSV file containing information about the stations and
     * populate the TreeMap<String,Station> stationList. Each row of
     * the CSV file contains the name, latitude and longitude coordinates
     * of the station.
     *
     * You need to make a Station object for each row and add it to the
     * TreeMap<String,Station> stationList where the key value is the
     * name of the station (as a String).
     *
     * @param infile       the path to the file
     * @throws IOException if the file is not found
     */
    public void readStationData(String infile) throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(infile));
        String line = in.readLine();
        while((line=in.readLine())!=null) {
            String words[] = line.split(",");
            String stationName = words[0];
            String stationLatitude = words[1];
            String stationLongitude = words[2];
           
            Station newStop = new Station(stationName, Double.parseDouble(stationLatitude),Double.parseDouble(stationLongitude));
            stationList.put(stationName,newStop);
        }
        in.close();
    }
 
    /**
     * Reads the CSV file containing information about connectivity between
     * adjacent stations, and update the stations in stationList so that each
     * Station object has a list of adjacent stations.
     *
     * Each row contains two Strings separated by comma. To obtain the distance
     * between the two stations, you need to use the latitude and longitude
     * coordinates together with the computeDistance() methods defined below
     *
     * @param infile       the path to the file
     * @throws IOException if the file is not found
     */
    public void readConnectionData(String infile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(infile));
        String s;
        while ((s = in.readLine()) != null)
        {
            String[] data = s.split(",");
            if (!data[0].isEmpty() && !data[1].isEmpty())
            {
                //assume each pair is unique
                int distance = computeDistance(data[0], data[1]);
                stationList.get(data[0]).addNeighbour(stationList.get(data[1]), distance);
                stationList.get(data[1]).addNeighbour(stationList.get(data[0]), distance);
            }
        }
        in.close();
    }
   
    /**
     * Given the latitude and longitude coordinates of two locations x and y,
     * return the distance between x and y in metres using Haversine formula,
     * rounded down to the nearest integer.
     *
     * Note that two more methods are provided below for your convenience
     * and you should not directly call this method
     *
     * source://www.geeksforgeeks.org/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
     *
     * @param lat1 latitude coordinate of x
     * @param lon1 longitude coordinate of x
     * @param lat2 latitude coordinate of y
     * @param lon2 longitude coordinate of y
     * @return distance betwee
     */
    public static int computeDistance(double lat1, double lon1, double lat2, double lon2)
    {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +  
                   Math.pow(Math.sin(dLon / 2), 2) *  
                   Math.cos(lat1) *  
                   Math.cos(lat2);
        double rad = 6371.0;
        Double c = 2 * Math.asin(Math.sqrt(a));
        Double distance = rad * c * 1000;
        return distance.intValue();
    }  
   
    /**
     * Compute the distance between two stations in metres, where the stations
     * are given as String objects
     *
     * @param a     the first station
     * @param b     the second station
     * @return      the distance between the two stations in metres
     */
    public int computeDistance(String a, String b)
    {
        Station u = stationList.get(a);
        Station v = stationList.get(b);
        return computeDistance(u.getLatitude(),u.getLongitude(),
                               v.getLatitude(),v.getLongitude());
    }
   
    /**
     * Compute the distance between two stations in metres, where the stations
     * are given as Station objects
     *
     * @param a     the first station
     * @param b     the second station
     * @return      the distance between the two stations in metres
     */
    public int computeDistance(Station a, Station b)
    {
        return computeDistance(a.getLatitude(),a.getLongitude(),
                               b.getLatitude(),b.getLongitude());
    }
 
   
   
   
    public Station nearestStation(TreeMap<Station, Integer> collection) {
        int smallest = Integer.MAX_VALUE;
        Station nearestStation = null; 
        for(Station s : collection.keySet()) {
                if(!s.isMarked() && collection.get(s)<smallest) {
                    smallest = collection.get(s);
                    nearestStation = s;
                }
                   
            }
        return nearestStation;
    }
   
   
   
    public void unMarkAll() {
        for(Station s: stationList.values())
            s.setUnmarked();
    }
   
   
    /**
     * The method finds the shortest route (in terms of distance travelled)
     * between the origin station and the destination station.
     * The route is returned as an ArrayList<String> containing the names of
     * the stations along the route, including the origin and the destination
     * stations.
     *
     * If the route cannot be completed (there is no path between origin and
     * destination), then return an empty ArrayList<String>
     *
     * If the origin or the destination stations are not in the list of stations,
     * return an empty ArrayList<String>.
     *
     * If the origin and the destination stations are the same, return an
     * ArrayList<String> containing the station.
     *
     * @param origin        the starting station
     * @param destination   the destination station
     * @return
     */
    public int routeMinDistance(String origin, String destination){
        if (!stationList.containsKey(origin) || !stationList.containsKey(destination)) {
            return 0;
        }
        if (origin.equals(destination)) {
            ArrayList<String> ans = new ArrayList<String>();
            ans.add(origin);
            return 0;
        }
       
       
       
        /*
         * INSERT YOUR CODE HERE
         */
        ArrayList<String> result = new ArrayList<>();
        Queue<Station> q = new LinkedList<Station>();
        TreeMap<Station, Integer> distance = new TreeMap<>();
        TreeMap<Station, Station> prevStation = new TreeMap<>();
       
        for(Station s : stationList.values()) {
            if(s.equals(stationList.get(origin))) {
                distance.put(s,0);
                if(!getMinDistFromOrigin.containsKey(origin))
                getMinDistFromOrigin.put(origin,new TreeMap<String,Integer>());
               
            }
            else {
                distance.put(s,Integer.MAX_VALUE);
                prevStation.put(s,null);
            }
            q.add(s);
        }
        while(!q.isEmpty()) {
            Station u = nearestStation(distance);
            if(u==null || u.getName().equals(destination))
                break;
            q.remove(u);
            u.setMarked();
            if(u.getName().equals(destination))
                break;
            Iterator<Station> iter = u.getAdjacentStations().keySet().iterator();
            while(iter.hasNext()) {
                Station neighbour= iter.next();
                if(q.contains(neighbour)) {
                    int alt = distance.get(u) + u.getAdjacentStations().get(neighbour);
                    if(alt < distance.get(neighbour)) {
                        distance.put(neighbour,alt);
                        getMinDistFromOrigin.get(origin).put(neighbour.getName(),distance.get(neighbour));
                       
                        if(!getMinDistFromOrigin.containsKey(neighbour.getName())) getMinDistFromOrigin.put(neighbour.getName(),new TreeMap<String,Integer>());
                        getMinDistFromOrigin.get(neighbour.getName()).put(origin,distance.get(neighbour));
                       
                       
                       
                        prevStation.put(neighbour,u);
                    }
                   
                }
            }
        }
        System.out.println("Distance between " + origin + " and " + destination + " is " + distance.get(stationList.get(destination)).toString());
        return getMinDistFromOrigin.get(origin).get(destination);
}
   
   
   
   
    /**
     * Given a route between two stations, compute the total distance
     * of this route.
     *
     * @param path  the list of stations in the route (as String objects)
     * @return      the length of the route between the first station
     *              and last station in the list   
     */
    public int findTotalDistance(ArrayList<String> path)
    {
        if (path == null || path.isEmpty())
            return 0;
 
        int distance = 0;
 
        for (int stationNo = 0; stationNo < path.size() - 1; stationNo++)
        {
            Station current = stationList.get(path.get(stationNo));
            Station next = stationList.get(path.get(stationNo + 1));
 
            distance += current.getAdjacentStations().get(next);
        }
 
        return distance;
    }
    public int optimisedDistance(String origin,String destination) {
        if(getMinDistFromOrigin.containsKey(origin)) {
            if(getMinDistFromOrigin.get(origin).containsKey(destination))
                return getMinDistFromOrigin.get(origin).get(destination);
        }
        return routeMinDistance(origin,destination);
    }
   
 
   
    /**
     * Return the ratio between the length of the shortest route between two
     * stations (in terms of distance) and the actual distance between the
     * two stations (computed using computeDistance())
     *
     * In other words,
     * let d1 = distance of shortest route between the two stations as computed
     *          by the method routeMinStop() (from Stage 1).
     * let d2 = distance between two stations as computed by the method
     *          computeDistance()
     *          
     * The method returns d1/d2 (as a double)
     *
     * @param origin        the starting station
     * @param destination   the ending station
     * @return  s           the ratio d1/d2 as explained above
     */
    public double computeRatio(String origin, String destination) {
        return (double) optimisedDistance(origin,destination)/computeDistance(origin,destination);
    }
   
   
    /**
     * Return the ratio as computed by computeRatio() method for all
     * pairs of station in the rail network.
     *
     * The ratios should be stored in a HashMap<String,HashMap<String,Double>>,
     * that is, the ratio between station a and b can be obtained by calling
     *
     *    computeAllRatio().get(a).get(b)
     *
     * @return a hashmap containing the ratios
     */
    public HashMap<String,HashMap<String,Double>> computeAllRatio() {
        HashMap<String,HashMap<String,Double>> hMap = new HashMap<>();
        Set<String> keys = stationList.keySet();
        ArrayList<String> k = new ArrayList<String>(stationList.keySet()); // get all stations in a list
        for(int i=0;i<k.size();i++) {
            HashMap<String,Double> h = new HashMap<String,Double>();
            HashMap<String,Double> z = new HashMap<String,Double>();
            for(int j=i+1;j<k.size();j++) {
                //compute ratio of between station i and j
                double d = computeRatio(k.get(i),k.get(j));
                h.put(k.get(j),d);
                z.put(k.get(i),d);
                //ratio (i,j) == ratio(j,i)
                hMap.put(k.get(i),h);
                hMap.put(k.get(j),z);
            }
        }
        return hMap;
    }
   
    /**
     * The method finds the shortest route (in terms of number of stops)
     * between the origin station and the destination station, taking
     * into account the available routes in the rail network.
     *
     * The route is returned as an ArrayList<String> containing the lines taken,
     * any transfer between lines, and the names of the stations on each line,
     * including the origin and the destination stations.
     *
     * Please see the assignment specification for more information.
     *
     * If the route cannot be completed (there is no path between origin and
     * destination), then return an empty ArrayList<String>
     *
     * If the origin or the destination stations are not in the list of stations,
     * return an empty ArrayList<String>.
     *
     * If the origin and the destination stations are the same, return an
     * ArrayList<String> containing the station.
     *
     * @param origin        the starting station
     * @param destination   the end station
     * @return              the route taken
     */
 
    // !!!!WAS TESTING, NOT ACTUAL ALGORITHM!!!!
    public ArrayList<String> routeMinStopWithRoutes(String origin, String destination)
    {
        if (!stationList.containsKey(origin) || !stationList.containsKey(destination))
            return new ArrayList<>();
 
        //clear data sets
        pathFromOrigin.clear();
        distanceFromOrigin.clear();
 
        Station originStation = stationList.get(origin);
        Station destinationStation = stationList.get(destination);
 
        //add original station
        pathFromOrigin.put(originStation, new ArrayList<>(List.of(origin)));
 
        //launch recursion, true if track been found
        if (routeMinStopWithRoutesRec(originStation, destinationStation))
            return pathFromOrigin.get(destinationStation);
 
        //if recursion returned false, nothing was found, return empty list
        return new ArrayList<>();
    }
 
    private Boolean routeMinStopWithRoutesRec(Station origin, Station destination)
    {
        //if we're on dest already, return true
        if (origin.equals(destination))
            return true;
 
        //otherwise, set current origin to marked
        origin.setMarked();
 
        TreeMap<Station, Integer> neighbours = origin.getAdjacentStations();
 
        //modify distance to each neighbour
        for(Map.Entry<Station,Integer> neighbour : neighbours.entrySet())
        {
            if (!pathFromOrigin.containsKey(neighbour.getKey()) ||
                    pathFromOrigin.get(neighbour.getKey()).size() > pathFromOrigin.get(origin).size() + 1)
            {
                neighbour.getKey().setUnmarked();
 
                ArrayList<String> list = new ArrayList<>(pathFromOrigin.get(origin));
                list.add(neighbour.getKey().getName());
 
                pathFromOrigin.put(neighbour.getKey(), list);
            }
        }
 
        Boolean result = false;
 
        //call each neighbour
        for(Map.Entry<Station,Integer> neighbour : neighbours.entrySet())
        {
            if (!neighbour.getKey().isMarked())
                result |= routeMinStopWithRoutesRec(neighbour.getKey(), destination);
        }
        return result;
    }
   
    public static void main(String[] args)
    {
        String stationData = "codes/src/data/station_data.csv";
        String connectionData = "codes/src/data/adjacent_stations.csv";
        String linesData = "codes/src/data/lines_data.csv";
        RailNetworkAdvanced r = new RailNetworkAdvanced(stationData,connectionData,linesData);
 
    }
}