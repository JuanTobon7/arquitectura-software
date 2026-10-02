import { Test, TestingModule } from '@nestjs/testing';
import { CompProtocoloComunicacionService } from './comp-protocolo-comunicacion.service.js';

describe('CompProtocoloComunicacionService', () => {
  let service: CompProtocoloComunicacionService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CompProtocoloComunicacionService],
    }).compile();

    service = module.get<CompProtocoloComunicacionService>(CompProtocoloComunicacionService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
