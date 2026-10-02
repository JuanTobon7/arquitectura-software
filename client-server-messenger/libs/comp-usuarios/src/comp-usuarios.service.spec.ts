import { Test, TestingModule } from '@nestjs/testing';
import { CompUsuariosService } from './comp-usuarios.service.js';

describe('CompUsuariosService', () => {
  let service: CompUsuariosService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CompUsuariosService],
    }).compile();

    service = module.get<CompUsuariosService>(CompUsuariosService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
