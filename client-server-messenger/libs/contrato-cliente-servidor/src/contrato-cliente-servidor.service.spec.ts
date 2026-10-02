import { Test, TestingModule } from '@nestjs/testing';
import { ContratoClienteServidorService } from './contrato-cliente-servidor.service.js';

describe('ContratoClienteServidorService', () => {
  let service: ContratoClienteServidorService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ContratoClienteServidorService],
    }).compile();

    service = module.get<ContratoClienteServidorService>(ContratoClienteServidorService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
