package com.springbatch.BatchProcessing.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.data.RepositoryItemWriter;
import com.springbatch.BatchProcessing.entity.Customer;
import com.springbatch.BatchProcessing.repository.CustomerRepository;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
@Configuration
@EnableBatchProcessing

public class SpringBatchConfig {
//	@Autowired
//	private JobBuilderFactory jobBuilderFactory;
//	@Autowired
//	private StepBuilderFactory stepBuilderFactory

	
	 private final JobRepository jobRepository;
	    private final PlatformTransactionManager transactionManager;

	    @Autowired
	    public SpringBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
	        this.jobRepository = jobRepository;
	        this.transactionManager = transactionManager;
	    }

	   
	@Autowired
	private CustomerRepository customerRepository;
	
	@Bean
	public FlatFileItemReader<Customer>reader(){
		
		FlatFileItemReader<Customer>itemReader=new FlatFileItemReader();
		
	    itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
	    itemReader.setName("csvReader");
	    
	    itemReader.setLinesToSkip(1);
	    itemReader.setLineMapper(lineMapper());
	    return itemReader;
	    
	}
	public LineMapper<Customer>lineMapper(){
		DefaultLineMapper<Customer>lineMapper=new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
	    lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");
	    
	    BeanWrapperFieldSetMapper<Customer>fieldSetMapper=new BeanWrapperFieldSetMapper<>();
	    fieldSetMapper.setTargetType(Customer.class);
	    lineMapper.setLineTokenizer(lineTokenizer);
	lineMapper.setFieldSetMapper(fieldSetMapper);
	return lineMapper;
	}
	@Bean
	public CustomerProcessor processor() {
		return new CustomerProcessor();
	}
	@Bean
	public RepositoryItemWriter<Customer>writer(){
		RepositoryItemWriter<Customer>writer=new RepositoryItemWriter<>();
		writer.setRepository(customerRepository);
	writer.setMethodName("save");
	return writer;
	}
	@Bean
		 public Step step1() {
		        return new StepBuilder("csv.step1", jobRepository)
		                .<Customer, Customer>chunk(10, transactionManager)
		                .reader(reader())
		                .processor(processor())
		                .writer(writer())
		                .taskExecutor(taskExecutor())
		                .build();
		    }
	 @Bean
	    public TaskExecutor taskExecutor() {
	        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
	        asyncTaskExecutor.setConcurrencyLimit(10);//10 threads at once for better performance
	        return asyncTaskExecutor;
	    }
	
	
	@Bean
	public Job runJob(JobRepository jobRepository ) {
		return new JobBuilder("importCustomers",jobRepository).flow(step1()).end().build();
		
	}
	
	

}
