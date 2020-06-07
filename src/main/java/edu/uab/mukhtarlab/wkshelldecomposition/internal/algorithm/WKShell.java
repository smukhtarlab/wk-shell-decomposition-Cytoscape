package edu.uab.mukhtarlab.wkshelldecomposition.internal.algorithm;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class WKShell
{
    public ArrayList<String> decompose(Graph G)
    {
        int old_min = Integer.MIN_VALUE;
        int shl = 1;
        double alpha = 0.5;
        ArrayList<String> weight_edges = new ArrayList<String>();
        ArrayList<String> shells = new ArrayList<String>();
        //2-Calculate edge weight
        int e=0;
        int wgt;
        for(int i=1;i<=G.V();i++)
        {
            for(String w : G.adjacentTo(""+i))
            {
                wgt = G.degree(""+i) + G.degree(""+w);
                String s = i+","+w+","+wgt;
                String s1 = w+","+i+","+wgt;
                if(!(weight_edges.contains(s) || weight_edges.contains(s1)))
                {
                    weight_edges.add(s);
                    e++;
                }
            }
        }
        int[][] w_e = new int[weight_edges.size()+1][4];
        int edge = 1;
        for(String s : weight_edges)
        {
            StringTokenizer stoken = new StringTokenizer(s,",");
            w_e[edge][0] = Integer.parseInt(stoken.nextToken());
            w_e[edge][1] = Integer.parseInt(stoken.nextToken());
            w_e[edge][2] = Integer.parseInt(stoken.nextToken());
            w_e[edge][3] = 0;
            edge++;
        }
        int[][] n_w = new int[G.V()][2];
        do{
            //3-Calculate Weighted Degree for Each Node
            for(int i=1;i<=n_w.length;i++)
            {
                int we = 0;
                if(G.hasVertex(""+i))
                {
                    for(String w : G.adjacentTo(""+i))
                    {
                        if(G.hasVertex(w))
                        {
                            we+= G.weight(""+i,""+w);

                        }
                        for(int j=1;j<w_e.length;j++)
                        {
                            if(w_e[j][0]==i && w_e[j][1]==Integer.parseInt(w))
                            {
                                w_e[j][2] = G.weight(""+i,""+w);
                                w_e[j][3]=1;
                                break;
                            }
                        }

                    }
                    for(int j=1;j<w_e.length;j++)
                    {
                        if((w_e[j][0]==i || w_e[j][1]==i)&& w_e[j][3]==0)
                            we+=w_e[j][2];
                    }

                    n_w[i-1][0] = i;
                    n_w[i-1][1] = (int)(alpha*G.degree(""+i) + (1-alpha)*we);
                    //System.out.println(n_w.length);
                    //System.out.println(w_e.length);
                }//-endif vertex found
            }
            //edge_v1,edge_v2,edge_w,still exists=1
            /*System.out.println("******************Edge Weights******************");
            for(int x=0;x<w_e.length;x++)
                System.out.println(w_e[x][0]+"\t"+w_e[x][1]+"\t"+w_e[x][2]+"\t"+w_e[x][3]);
            System.out.println("******************Node Degree Weights******************");
            for(int i=0;i<n_w.length;i++)
                System.out.println(n_w[i][0]+"\t"+n_w[i][1]);
            System.out.println("--------------------------------------------------------");*/
            //4-Find Lowest n_w node weight
            int min = Integer.MAX_VALUE;
            for(int i=0;i<n_w.length;i++)
            {
                //if(G.hasVertex(""+i))
                {
                    if(n_w[i][1] < min)
                        min = n_w[i][1];
                }
            }
            //System.out.println(min);
            //5- Remove it in a shell
            //System.out.println(G.V());
            String sh = "";
            for(int i=0;i<n_w.length;i++)
            {
                if(n_w[i][1]==min)
                {
                    sh +=n_w[i][0]+",";
                    G.deleteVertex((i+1)+"");
                    for(int j=0;j<w_e.length;j++)
                    {
                        if(w_e[j][0]==(i+1) || w_e[j][1]==(i+1))
                            w_e[j][3]=0;
                    }
                }
            }

            //This to fix issue of getting a lower node weight value after a bigger one
            String ss;
            if(min <= old_min)
            {
                min = old_min;
                ss=shells.get(shells.size()-1);
                shells.remove(shells.size()-1);
                ss+=sh;
                sh=ss;
            }
            //sh+=min+","+shl;
            //sh = sh.substring(0, sh.length() - 1);
            shells.add(sh);
            //System.out.println("("+shl+")-"+sh+"==>"+min);
            //shl++;
            //System.out.println("-------------------------------------");
            sh="";
            //System.out.println(G.V());

            for(int i=0;i<n_w.length;i++)
                n_w[i][1] = Integer.MAX_VALUE;
            old_min = min;
        }while(G.V() != 0);

        return shells;
    }
}