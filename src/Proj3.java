import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Proj3 {

    static heap myHeap = new heap(5000);
    static int edgeCount;
    static upTree[] upTrees = new upTree[1000];
    static List<edge> MST = new ArrayList<>();

    static vertex[] myAdjacencyList = new vertex[1000];
    static String inputFilePath;
    static String outputFilePath;

    public static void main(String[] args) {
        BufferedReader br;
        BufferedWriter bw;



        try {
            br = new BufferedReader(new InputStreamReader(System.in));

//            inputFilePath = "\\C:\\Users\\driedell\\Desktop\\CSC316\\Proj3\\2.txt";
//            outputFilePath = "\\C:\\Users\\driedell\\Desktop\\CSC316\\Proj3\\3.txt";
            System.out.print("Enter input file: ");
            inputFilePath = br.readLine();
            if (inputFilePath.contains("\"")) {
                inputFilePath = inputFilePath.replace("\"", "");
            }

            System.out.println("Enter output file: ");
            outputFilePath = br.readLine();
            if (outputFilePath.contains("\"")) {
                outputFilePath = outputFilePath.replace("\"", "");
            }


            System.out.println("Input File: " + inputFilePath);
            System.out.println("Output File: " + outputFilePath);

            File file = new File(outputFilePath);
            if (file.exists()) {
                file.delete();
            }

            br = new BufferedReader(new FileReader(inputFilePath));



            String input;
            while ((input = br.readLine()) != null) {
                buildGraph(input);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        int vertexCount = 0;
        for (int i = 0; i < myAdjacencyList.length; i++) {
            if (myAdjacencyList[i] != null) {
                vertexCount++;
            }
        }


        try {
            bw = new BufferedWriter(new FileWriter(outputFilePath, true));

            System.out.println("Vertex count: " + vertexCount);
            for (int i = 0; i < myHeap.size; i++) {
                System.out.println(String.format("%4s", myHeap.edges[i].vertex1.key) + " " +
                        String.format("%4s", myHeap.edges[i].vertex2.key));

                bw.write(String.format("%4s", myHeap.edges[i].vertex1.key) + " " +
                        String.format("%4s", myHeap.edges[i].vertex2.key) + "\n");
            }

            while (myHeap.size > 0) {
                edge myEdge = myHeap.deleteMin();
                upTree U = upTrees[myEdge.vertex1.key].find();
                upTree V = upTrees[myEdge.vertex2.key].find();
                if (U != V) {
                    U.union(U, V);
                    MST.add(myEdge);
                }
            }

            Collections.sort(MST);
            System.out.println("MST: ");
            for (edge e : MST) {
                System.out.println(String.format("%4s", e.vertex1.key) + " " + String.format("%4s", e.vertex2.key));
                bw.write(String.format("%4s", e.vertex1.key) + " " + String.format("%4s", e.vertex2.key) + "\n");
            }

            System.out.println("Adjacency list: ");
            String printString;
            int j = 0;
            while (myAdjacencyList[j] != null) {
            System.out.print(String.format("%4s", myAdjacencyList[j].key) + ": ");
                printString = "";

                for (edge e : myAdjacencyList[j].edges) {
                    if (e.vertex1 == myAdjacencyList[j]) {
                        printString += String.format("%4s", e.vertex2.key);
                    } else {
                        printString += String.format("%4s", e.vertex1.key);
                    }
                    printString += " ";
                }

                System.out.println(printString.substring(0, printString.length() - 1));
                bw.write(printString.substring(0, printString.length() - 1) + "\n");
                j++;
            }
            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Split line into int vertexes and double weight
    // check if first vertex exists, if not create it
    // check if second vertex exists, if not create it
    // add connections
    // create edge
    // insert edge into heap
    public static void buildGraph(String input) {
        Pattern p = Pattern.compile("\\d+(\\.\\d)?");
        Matcher m;

//        String[] myStrings = input.split("\n");
        int j = 0;
        if (input.contains("-1")) {
            return;
        }
        edgeCount++;

        m = p.matcher(input);
        String[] result = new String[3];
        int k = 0;
        while (m.find()) {
            result[k] = m.group();
            k++;
        }


        int left = Integer.parseInt(result[0]);
        int right = Integer.parseInt(result[1]);
        double weight = Double.parseDouble(result[2]);
        vertex l = new vertex(left, right);
        vertex r = new vertex(right, left);

        if (myAdjacencyList[left] != null) {
            myAdjacencyList[left].addConnection(right);
        } else {
            myAdjacencyList[left] = l;
            upTrees[l.key] = new upTree(l);
        }
        if (myAdjacencyList[right] != null) {
            myAdjacencyList[right].addConnection(left);
        } else {
            myAdjacencyList[right] = r;
            upTrees[r.key] = new upTree(r);
        }

        edge e;
        if (l.key < r.key) {
            myHeap.insert(e = new edge(myAdjacencyList[left], myAdjacencyList[right], weight));
        } else {
            myHeap.insert(e = new edge(myAdjacencyList[right], myAdjacencyList[left], weight));
        }
        e.vertex1.addEdgeConnections(e);
        e.vertex2.addEdgeConnections(e);
        j++;
    }

}

class heap {
    int capacity;
    int size;
    public edge[] edges;

    public heap(int capacity) {
        edges = new edge[capacity];
        this.capacity = capacity;
        this.size = 0;
    }

    public void insert(edge e) {
        edges[size] = e;
        size++;
        upHeap(size - 1);
    }

    public void swap(int parent, int child) {
        edge temp = edges[parent];
        edges[parent] = edges[child];
        edges[child] = temp;
    }

    private void upHeap(int i) {
        if (i > 0) {
            if (edges[(i - 1) / 2].weight > edges[i].weight) {
                swap((i - 1) / 2, i);
                upHeap((i - 1) / 2);
            }
        }
    }

    private void downHeap(int m) {
        int i = 0;
        if (2 * m + 2 < size) {
            if (edges[2 * m + 2].weight <= edges[2 * m + 1].weight) {
                i = 2 * m + 2;
            } else {
                i = 2 * m + 1;
            }
        } else if (2 * m + 1 < size) {
            i = 2 * m + 1;
        }

        if (i > 0 && edges[m].weight > edges[i].weight) {
            swap(m, i);
            downHeap(i);
        }
    }

    public edge deleteMin() {
        edge e = edges[0];
        size--;
        swap(0, size);
        downHeap(0);
        return e;
    }
}

class upTree {
    vertex v;
    upTree parent;
    int count;

    public upTree(vertex v) {
        this.v = v;
        this.parent = null;
        this.count = 1;
    }

    public upTree union(upTree s, upTree t) {
        if (s.count >= t.count) {
            s.count += t.count;
            t.parent = s;
            return s;
        } else {
            t.count += s.count;
            s.parent = t;
            return t;
        }
    }

    public upTree find() {
        upTree u = this;
        while (u.parent != null) {
            u = u.parent;
        }
        return u;
    }


    public upTree find(upTree u) {
        while (u.parent != null) {
            u = u.parent;
        }
        return u;
    }

}

class edge implements Comparable<edge> {
    public vertex vertex1 = new vertex();
    public vertex vertex2 = new vertex();
    public double weight;
    public edge next;

    public edge(vertex vertex1, vertex vertex2, double weight) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.weight = weight;
    }

    @Override
    public int compareTo(edge e) {
        if (this.vertex1.key > e.vertex1.key) {
            return 1;
        } else if (this.vertex1.key == e.vertex1.key) {
            if (this.vertex2.key > e.vertex2.key) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}

class vertex {
    public int key;

    public List<Integer> connections = new ArrayList<>();

    public List<edge> edges = new ArrayList<>();

    public void addEdgeConnections(edge e) {
        this.edges.add(e);
        Collections.sort(this.edges);
    }

    public ArrayList<vertex> vertexConnections = new ArrayList<>();

    // constructor
    public vertex() {

    }

    public vertex(int key) {
        this.key = key;
    }

    public vertex(int key, int connection) {
        this.key = key;
        this.connections.add(connection);
    }

    public void addConnection(int connection) {
        this.connections.add(connection);
        Collections.sort(this.connections);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!vertex.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final vertex v = (vertex) obj;

        if (this.key != v.key) {
            return false;
        }
        return true;
    }
}