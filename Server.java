package aroundearth;

public class Server implements Runnable{
	
	public int port;
	
	public Server(int port) {
		this.port = port;
	}
	// client�� �޾� hashmap<String, Socket>�� �߰�
	public void waitForClient() {
		
	}
	
	public int getVoting() {
		
		return 0;
	}
	
	public void setSkrull() {
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(int i = 0 ; i < 5; ++i) {
			waitForClient(); 
		}
		
		setSkrull();
		
		
		while(true) {

			// Client���� ä�� �ð� �ο�
			
			getVoting();
			
			// Game ���� Ȯ��
			
		}
	}
	
	public void sendMessage() {
		
	}
	
	//-------------------------------------------
	class ChatThread implements Runnable{

		@Override
		// ������� ���⼭ ó��
		// ������ ��� �����ֱ⸸ �ϸ� �ȴ�.
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}

