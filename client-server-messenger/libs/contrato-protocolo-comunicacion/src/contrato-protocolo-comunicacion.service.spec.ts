import { Test, TestingModule } from '@nestjs/testing';
import { ContratoProtocoloComunicacionService } from './contrato-protocolo-comunicacion.service.js';

describe('ContratoProtocoloComunicacionService', () => {
  let service: ContratoProtocoloComunicacionService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ContratoProtocoloComunicacionService],
    }).compile();

    service = module.get<ContratoProtocoloComunicacionService>(ContratoProtocoloComunicacionService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
