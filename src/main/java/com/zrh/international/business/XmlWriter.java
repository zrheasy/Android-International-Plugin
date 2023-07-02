package com.zrh.international.business;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @author zrh
 * @date 2022/7/30
 */
class XmlWriter {


    public void write(Map<String, String> updateList, File inputFile) throws Exception {
        if (updateList.isEmpty()) return;

        Document doc = openDoc(inputFile);
        // 获取根节点
        Node resources = doc.getFirstChild();
        if (resources == null) {
            resources = doc.createElement("resources");
            doc.appendChild(resources);
        }

        // 获取所有的string
        NodeList nodeList = resources.getChildNodes();
        int count = nodeList.getLength();
        HashMap<String, Element> elements = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element stringElement = (Element) node;
                if (stringElement.getTagName().equals("string")) {
                    String name = stringElement.getAttribute("name");
                    elements.put(name, stringElement);
                }
            }
        }

        boolean hasNew = false;
        // 修改string
        for (String name : updateList.keySet()) {
            boolean isNew = setElement(doc, resources, elements, name, updateList.get(name));
            if (isNew)hasNew = true;
        }
        if (hasNew){
            Text textNode = doc.createTextNode("\n");
            resources.appendChild(textNode);
        }

        // 将修改的数据写入xml文件
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        DOMSource source = new DOMSource(doc);

        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            transformer.transform(source, new StreamResult(outputStream));
        }
    }

    private Document openDoc(File inputFile) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        if (!inputFile.exists()) {
            File dir = new File(inputFile.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            inputFile.createNewFile();

            return docBuilder.newDocument();
        }

        try (InputStream inputStream = new FileInputStream(inputFile)) {
            return docBuilder.parse(inputStream);
        }
    }

    /**
     * 更新节点数据，如果存在节点则直接更新，不存在则创建新节点
     *
     * @return 是否创建新节点
     */
    private boolean setElement(Document doc, Node parent, Map<String, Element> elements, String name, String value) {
        Element element = elements.get(name);
        if (element != null) {
            element.setTextContent(value);
            return false;
        } else {
            // 换行
            Text textNode = doc.createTextNode("\n    ");
            parent.appendChild(textNode);

            element = doc.createElement("string");
            element.setAttribute("name", name);
            element.setTextContent(value);
            parent.appendChild(element);
            return true;
        }
    }
}
