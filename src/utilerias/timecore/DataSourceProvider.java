package utilerias.timecore;

import com.inga.utils.SigarUtils;
import cryptowerk.Cryptowerk;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import timesoft.Main;

public class DataSourceProvider extends DriverManagerDataSource
{
  private Properties thProps;
  private static Logger log = Logger.getLogger(DataSourceProvider.class.getName());

  public DataSourceProvider()
  {
      
    try 
    {
        Properties props = SigarUtils.loadProperties("config.properties");
        String miDriver = props.getProperty("driver");
        setDriverClassName( miDriver );
        log.info("Driver: " + miDriver );
    }
    catch ( Exception ex )
    {
        log.error( SigarUtils.stackTraceString(ex) );
    }
    
    try
    {
      this.thProps = Cryptowerk.readProperties("timesoft.properties", Main.KEY);
    }
    catch (Exception ex)
    {
    }
    
    
  }

  @Override
  public String getUsername()
  {
    return this.thProps.getProperty("user");
  }

  @Override
  public String getPassword()
  {
    return this.thProps.getProperty("password");
  }

  @Override
  public String getUrl()
  {
    return this.thProps.getProperty("url");
  }
}