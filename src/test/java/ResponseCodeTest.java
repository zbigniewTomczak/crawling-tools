import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ResponseCodeTest {
	
	@Test
	public void fileCode200() throws IOException {
		final String fileName = "target/test-classes/code.200";
		final String codeOk = "200";
		File file = new File(fileName);
		if (! file.exists() ) {
			Assert.fail(String.format("File %s does not exist.",file.toString()));
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		boolean fail = false;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty()) continue;
			if (line.charAt(0) == '#') continue;
			String codeReturn = ResponseCode.responseCode(line);
			if (! codeReturn.equals(codeOk)) {
				System.out.format("%s %s", line, codeReturn);
				fail = true;
			}
		}
		if (fail) {
			Assert.fail("Not all addresses returned status: " + codeOk + ". See console log for detail.");
		}
	}
}