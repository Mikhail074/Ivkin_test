import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, ParseException, SAXException {
        //Параметры
        String objectId = "100265562, 1419964, 1451686, 1453785, 1422127";
        String parseDate = "2005-02-24";
        String obj = "C:\\\\Users\\\\Михаил\\\\Documents\\\\AS_ADDR_OBJ.xml";
        String hierarhy = "C:\\\\Users\\\\Михаил\\\\Documents\\\\AS_ADM_HIERARCHY.xml";

        System.out.println("Результат для первой задачи:");
        printObjectName(objectId, parseDate,obj);

        System.out.println("Результат для второй задачи:");
        printFullAddress(obj, hierarhy);

    }

    private static void printObjectName(String objectId, String dateParam, String obj) throws ParserConfigurationException, IOException, SAXException, ParseException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(obj));


        String[] id = objectId.split(", ");

        Element element = document.getDocumentElement();
        HashMap<String,String> map = getObjectName(element.getChildNodes(), dateParam);

        for (String s : id) {
            String objName = map.get(s);
            if (objName != null)
                System.out.println(s + ": " + objName);
        }
    }
    private static void printFullAddress(String obj,String hierarhy) throws ParserConfigurationException, IOException, SAXException, ParseException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File("C:\\Users\\Михаил\\Documents\\AS_ADM_HIERARCHY.xml"));

        Element element = document.getDocumentElement();
        HashMap<String,String> parentsMap = getActualParentsMap(element.getChildNodes());

        Document documentStr = builder.parse(new File(obj));
        Element elementStr = documentStr.getDocumentElement();

        printFullAddressEnd(elementStr.getChildNodes(), parentsMap);
    }

    private static HashMap<String,String> getObjectName(NodeList nodeList, String dateParam) throws ParseException {

        HashMap<String,String> hashMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element){
                if (((Element) nodeList.item(i)).hasAttribute("STARTDATE")) {
                    String strDate = ((Element) nodeList.item(i)).getAttribute("STARTDATE");
                    Date date = format.parse(strDate);
                    Date dateCompare = format.parse(dateParam);
                    if (date.compareTo(dateCompare) == 0){
                        hashMap.put(((Element) nodeList.item(i)).getAttribute("OBJECTID").toString(),
                                ((Element) nodeList.item(i)).getAttribute("NAME").toString());
                    }
                }
            }
        }
        return hashMap;
    }
    private static HashMap<String,String> getActualParentsMap(NodeList nodeList) throws ParseException {
        HashMap<String,String> hashMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element){
                if (((Element) nodeList.item(i)).hasAttribute("STARTDATE")) {
                    String strDate = ((Element) nodeList.item(i)).getAttribute("STARTDATE");
                    Date date = format.parse(strDate);
                    String currentObjectId = ((Element) nodeList.item(i)).getAttribute("OBJECTID");
                    String s = hashMap.get(currentObjectId);
                    Date currentDate = date;
                    if (s != null) {
                        currentDate = format.parse(s.substring(0, 9));
                    }
                    if (s == null || date.compareTo(currentDate)>0)  {
                        //String resDate = ((Element) nodeList.item(i)).getAttribute("STARTDATE");
                        String resParentId = ((Element) nodeList.item(i)).getAttribute("PARENTOBJID");
                        hashMap.put(currentObjectId, strDate + ":" + resParentId);
                    }
                }
            }
        }
        return hashMap;
    }

    private  static void printFullAddressEnd(NodeList nodeList, HashMap<String, String> parentsMap){
        HashMap<String,String> addressMap = new HashMap<>();
        ArrayList<String> findId = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element){
                if (((Element) nodeList.item(i)).hasAttribute("OBJECTID")) {
                    String id = ((Element) nodeList.item(i)).getAttribute("OBJECTID");
                    String name = ((Element) nodeList.item(i)).getAttribute("NAME");
                    String typename = ((Element) nodeList.item(i)).getAttribute("TYPENAME");
                    addressMap.put(id,typename + " " + name);
                    if (typename.equals("проезд")){
                        findId.add(id);
                    }
                }
            }
        }

        for (int i = 0; i < findId.size(); i++) {
            System.out.println(getFullAddress(findId.get(i), addressMap, parentsMap,""));
        }
    }

    public static  String getFullAddress(String id, HashMap<String,String>addressMap, HashMap<String,String>parentsMap, String result){
        String parentName = (addressMap.get(id) == null ? "": addressMap.get(id));
        result =  parentName + " " + result;
        String parent = parentsMap.get(id);

        if (parent == null)
            return result;
        else{
            String[] parentid = parent.split(":");
            return(getFullAddress(parentid[1],addressMap,parentsMap, result));
        }
    }
}