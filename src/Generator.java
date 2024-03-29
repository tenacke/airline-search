import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

public class Generator {

    double ratio;
    public static double total_distance = 0;
    public static double total_edges = 0;

    public Generator(double ratio){
        this.ratio = ratio;
    }


    private boolean connected(Main.Node node1, Main.Node node2){
        if (node1.id().equals(node2.id())) return true;
        if (node1.adjacents().contains(node2) && node2.adjacents().contains(node1)) return true;
        return false;
    }

    private void connect(Main.Node node1, Main.Node node2){
        node1.adjacents().add(node2);
        node2.adjacents().add(node1);
        double dist = Main.haversine(node1.lat(), node1.lon(), node2.lat(), node2.lon());
        node1.distance().add(dist);
        node2.distance().add(dist);
        total_distance += dist;
        total_edges++;
    }



    public void generate(Object[] nd){
   
        List<Object> nodes = new ArrayList<>(Arrays.asList(nd));
        int size = nodes.size();

        int num_edges = 0;

        int num_selected = 0;
        boolean[] selected = new boolean[size];
        for (int i = 0; i < size; i++){
            selected[i] = false;
        }

        Random rand = new Random();


        int src = 0;

        // generate spanning tree
        while (num_selected < size){
            if (num_selected == 0){
                src = rand.nextInt(size);
                selected[src] = true;
                num_selected++;
            }
            int dst = rand.nextInt(size);
            if (selected[dst] || src == dst) continue;

            selected[dst] = true;

            Main.Node source = (Main.Node) nodes.get(src);
            Main.Node dest = (Main.Node) nodes.get(dst);

            connect(source, dest);
            
            num_edges++;
            num_selected++;
            src = dst;
        }

        // add random edges
        double d = Math.pow((double)size, 2);
        while(num_edges / (d) < ratio){
            int srrc = rand.nextInt(size);
            int dst = rand.nextInt(size);
            Main.Node source = (Main.Node) nodes.get(srrc);
            Main.Node dest = (Main.Node) nodes.get(dst);
            if (connected(source, dest)){
                continue;
            }
            connect(source, dest);
            num_edges++;
        }
    }
}
