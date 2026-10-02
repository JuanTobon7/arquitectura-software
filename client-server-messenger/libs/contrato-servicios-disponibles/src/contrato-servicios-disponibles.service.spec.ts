import { Test, TestingModule } from '@nestjs/testing';
import { ContratoServiciosDisponiblesService } from './contrato-servicios-disponibles.service.js';

describe('ContratoServiciosDisponiblesService', () => {
  let service: ContratoServiciosDisponiblesService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ContratoServiciosDisponiblesService],
    }).compile();

    service = module.get<ContratoServiciosDisponiblesService>(ContratoServiciosDisponiblesService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
