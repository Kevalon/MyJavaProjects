import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

public class XSDValidator {

    public static boolean validate(String path)     {
        return validateXMLSchema("prng-config.xsd", path);
    }

    public static boolean validateXMLSchema(String xsdPath, String xmlPath)     {
        try {
            // Получить фабрику для схемы
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // Загрузить схему из XSD
            Schema schema = factory.newSchema(new File(xsdPath));
            // Создать валидатор
            Validator validator = schema.newValidator();
            // Запусить проверку - если будет исключение, значит есть ошибки.
            // Если нет - все заполнено правильно
            validator.validate(new StreamSource(new File(xmlPath)));
        } catch (IOException | SAXException e) {
            System.err.println("Exception: " + e.getMessage());
            return false;
        }
        return true;
    }
}
