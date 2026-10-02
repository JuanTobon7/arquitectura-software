import { Test, TestingModule } from '@nestjs/testing';
import { CompProtocoloClienteServidorService } from './comp-protocolo-cliente-servidor.service.js';

describe('CompProtocoloClienteServidorService', () => {
  let service: CompProtocoloClienteServidorService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CompProtocoloClienteServidorService],
    }).compile();

    service = module.get<CompProtocoloClienteServidorService>(CompProtocoloClienteServidorService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
