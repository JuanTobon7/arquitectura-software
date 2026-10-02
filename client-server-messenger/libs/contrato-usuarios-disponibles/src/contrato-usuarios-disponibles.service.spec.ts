import { Test, TestingModule } from '@nestjs/testing';
import { ContratoUsuariosDisponiblesService } from './contrato-usuarios-disponibles.service.js';

describe('ContratoUsuariosDisponiblesService', () => {
  let service: ContratoUsuariosDisponiblesService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ContratoUsuariosDisponiblesService],
    }).compile();

    service = module.get<ContratoUsuariosDisponiblesService>(ContratoUsuariosDisponiblesService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
