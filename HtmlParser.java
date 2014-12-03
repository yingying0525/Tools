package cn.ruc.mblank.core.peopleRelation;


import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import javax.print.Doc;
import java.io.*;

/**
 * Created by hp on 2014/11/4.
 */
public class HtmlParser
{

    public static void main(String[] args)
    {
        String FileDir = args[0];
        String DataDir = args[1];

        try
        {
            BufferedWriter bwV = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileDir + File.separator + "verify", true)));
            BufferedWriter bwP = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileDir + File.separator + "peopleName", true)));
            BufferedWriter bwN = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileDir + File.separator + "NoPage", true)));
            BufferedWriter bwNew = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileDir + File.separator + "NewPage", true)));
            BufferedWriter bwO = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileDir + File.separator + "OtherInfo", true)));
            BufferedWriter bwKey = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileDir + File.separator + "Keywords", true)));

            File fileDir = new File(DataDir);
            String []dir_sec = fileDir.list();
            for (String d : dir_sec)
            {
                System.out.println(d);
                File fileDir_sec = new File(DataDir + File.separator + d);
                String[] htmls = fileDir_sec.list();
                for (String html : htmls)
                {
                    File file = new File(DataDir + File.separator + d + File.separator + html);

                    Document Doc = Jsoup.parse(file,"UTF-8");
                    //404 notfound
                    if (Doc.html().contains("<form action=\"http://verify.baidu.com/verify\">") && Doc.html().contains("<img src=\"http://verify.baidu.com/cgi-bin/genimg"))
                    {
                        bwV.write(html);
                        bwV.newLine();
                        continue;
                    }
                    //no such page
                    if (Doc.title().equals("百度百科_全球最大中文百科全书"))
                    {
                        bwN.write(html);
                        bwN.newLine();
                        continue;
                    }

                    Elements elements = Doc.getElementsByAttributeValue("name", "Keywords");
                    if (elements.size() > 0)
                    {
                        Element e = elements.get(0);
                        String keywords = e.attr("content");
                        //System.out.println(keywords);
                        //List page
                        if (keywords.endsWith("baike "))
                        {
                            Element list = Doc.getElementById("lemma-list");
                            if (list == null)
                            {
                                //System.out.println("break!!");
                                continue;
                            }
                            Element ul = list.getElementsByTag("ul").get(0);

                            Elements as = ul.getElementsByTag("a");
                            for (Element a : as)
                            {
                                String t = a.text();
                                String href = a.attr("href");
                                bwNew.write(html + "\t" + t + "\t" + href);
                                bwNew.newLine();
                            }
                            continue;
                        }

                        Element dl = Doc.getElementById("catalog-holder-0");
                        if (dl == null)
                        {
                            bwO.write(html + "\t" + keywords + "\t" + "NO_catalog-holder-0");
                            bwO.newLine();
                            continue;
                        }
                        Elements items = dl.getElementsByClass("z-catalog-i1");
                        if (items.size() == 0)
                        {
                            bwO.write(html + "\t" + keywords + "\t" + "NO_z-catalog-i1");
                            bwO.newLine();
                            continue;
                        }
                        Element itemF = items.get(0);
                        Elements Fas = itemF.getElementsByTag("a");
                        if (Fas.size() == 0)
                        {
                            bwO.write(html + "\t" + keywords + "\t" + "NO_Fa");
                            bwO.newLine();
                            continue;
                        }
                        String textF = Fas.get(0).text();
                        int indF = keywords.indexOf(textF);
                        if (indF == -1)
                        {
                            bwO.write(html + "\t" + keywords + "\t" + "NO_indF");
                            bwO.newLine();
                            continue;
                        }

                        Element itemL = items.get(items.size() - 1);
                        Elements Las = itemL.getElementsByTag("a");
                        if (Las == null)
                        {
                            bwO.write(html + "\t" + keywords + "\t" + "NO_La");
                            bwO.newLine();
                            continue;
                        }
                        String textL = Las.get(0).text();
                        int indL = keywords.indexOf(textL);
                        if (indL == -1)
                        {
                            bwO.write(html + "\t" + keywords + "\t" + "NO_indL");
                            bwO.newLine();
                            continue;
                        }
                        indL = indL + textL.length();

                        if (indF > indL)
                        {
                            bwO.write(html + "\t" + keywords  + "\t" + "indF_indL");
                            bwO.newLine();
                            continue;
                        }
                        String content,otherName;
                        if (keywords.length() > indL)
                        {
                            indL = indL + 1;
                            content = keywords.substring(indF, indL);
                            otherName = keywords.substring(indL);
                        }
                        else
                        {
                            content = keywords.substring(indF);
                            otherName = "NoOtherName!";
                        }

                        Elements h1 = Doc.getElementsByClass("lemmaTitleH1");
                        String title;
                        if (h1.size() > 0)
                            title = h1.get(0).text();
                        else
                        {
                            title = Doc.title();
                            title = title.substring(0, title.length() - 5);
                        }
                        //System.out.println(title);

                        if (content.contains("经历") || content.contains("个人") || content.contains("生活") || content.contains("生平") || content.contains("家庭")
                                || content.contains("履历") || content.contains("事迹") || content.contains("职务") || content.contains("社会活动")
                                || content.contains("研究方向") || content.contains("研究领域") || content.contains("主要作品"))
                        {
                            bwP.write(html + "\t" + title + "\t" + otherName + "\t" + content);
                            bwP.newLine();
                            continue;
                        }
                    }
                }
            }
            bwV.close();
            bwP.close();
            bwN.close();
            bwNew.close();
            bwO.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
